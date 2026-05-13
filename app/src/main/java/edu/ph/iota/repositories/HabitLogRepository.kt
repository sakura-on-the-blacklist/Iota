package edu.ph.iota.repositories


import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore
import edu.ph.iota.models.Habit
import edu.ph.iota.models.HabitLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * HabitLogRepository handles everything that happens when the user
 * holds a pebble for 3 seconds:
 *
 *   1. Creates a HabitLog document  (/habits/{id}/logs/{date})
 *   2. Calculates the new streak from recent logs
 *   3. Writes the updated streak cache back to the Habit document
 *   4. Returns the new streak so AchievementActivity can show it
 *
 * All three writes happen in a single Firestore batch so they are
 * atomic — either all succeed or all fail together.
 *
 * Collection paths:
 *   /habits/{habitId}               ← streak cache updated here
 *   /habits/{habitId}/logs/{logId}  ← new log written here
 */
class HabitLogRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ── Log a habit ───────────────────────────────────────────────────────────

    /**
     * Called immediately when the 3-second hold completes.
     *
     * @param habit     The Habit that was just logged.
     * @param date      The date being logged (usually today, but can be past
     *                  for retroactive edits — up to 7 days back).
     * @param value     How much the user did (e.g. 1.0 for "1 time").
     * @param onSuccess Called with the updated Habit (streak fields filled in).
     *                  Pass this to AchievementActivity via Intent.
     * @param onFailure Called with an error message string.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun logHabit(
        habit: Habit,
        date: LocalDate = LocalDate.now(),
        value: Double = habit.goalValue,
        onSuccess: (updatedHabit: Habit, newStreak: Int) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val dateKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE)  // "2025-04-30"
        val habitRef = firestore.collection("habits").document(habit.habitId)
        val logsRef  = habitRef.collection("logs")

        // Step 1: check if already logged today (prevent duplicates)
        logsRef.whereEqualTo("date", dateKey)
            .limit(1)
            .get()
            .addOnSuccessListener { existing ->
                if (!existing.isEmpty) {
                    onFailure("You already logged this habit for $dateKey.")
                    return@addOnSuccessListener
                }

                // Step 2: fetch recent logs to calculate streak
                // We only need the last 90 days to cover any realistic streak
                val ninetyDaysAgo = date.minusDays(90)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)

                logsRef
                    .whereGreaterThanOrEqualTo("date", ninetyDaysAgo)
                    .orderBy("date")
                    .get()
                    .addOnSuccessListener { logsSnapshot ->

                        val loggedDates = logsSnapshot.documents
                            .mapNotNull { it.getString("date") }
                            .toMutableList()

                        // Add today's date (not yet in Firestore)
                        loggedDates.add(dateKey)

                        // Step 3: calculate streak
                        val newStreak = calculateStreak(
                            loggedDates  = loggedDates,
                            frequencyDays = habit.frequencyDays,
                            frequency    = habit.frequency,
                            asOf         = date
                        )

                        val newLongest  = maxOf(habit.longestStreak, newStreak)
                        val newTotal    = habit.totalLogs + 1
                        val completed   = value >= habit.goalValue

                        // Step 4: build updated habit
                        val updatedHabit = habit.copy(
                            currentStreak  = newStreak,
                            longestStreak  = newLongest,
                            lastLoggedDate = dateKey,
                            totalLogs      = newTotal
                        )

                        // Step 5: write log + updated streak cache in a batch
                        val logDocRef = logsRef.document()
                        val newLog = HabitLog(
                            logId     = logDocRef.id,
                            habitId   = habit.habitId,
                            date      = dateKey,
                            value     = value,
                            completed = completed
                        )

                        val batch = firestore.batch()
                        batch.set(logDocRef, newLog.toMap())
                        batch.update(habitRef, mapOf(
                            "currentStreak"  to newStreak,
                            "longestStreak"  to newLongest,
                            "lastLoggedDate" to dateKey,
                            "totalLogs"      to newTotal
                        ))

                        batch.commit()
                            .addOnSuccessListener {
                                onSuccess(updatedHabit, newStreak)
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.message ?: "Failed to save log.")
                            }

                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to fetch logs.")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to check existing log.")
            }
    }

    // ── Streak calculation ────────────────────────────────────────────────────

    /**
     * Counts how many consecutive *scheduled* days ending on [asOf]
     * have a log entry.
     *
     * "Scheduled" means:
     *   frequency == "daily"  → every day counts
     *   frequency == "custom" → only days in frequencyDays count
     *
     * A missed scheduled day breaks the streak.
     * Non-scheduled days are skipped (they don't break the streak).
     *
     * Example — habit scheduled MON/WED/FRI, logs on Mon + Wed:
     *   streak = 2 ✓ (Tue is not a scheduled day, so it's ignored)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateStreak(
        loggedDates: List<String>,
        frequencyDays: List<String>,
        frequency: String,
        asOf: LocalDate
    ): Int {
        val loggedSet = loggedDates.toSet()
        var streak = 0
        var current = asOf

        // Walk backwards from asOf, counting consecutive scheduled+logged days
        while (true) {
            val isScheduled = when (frequency) {
                "daily"  -> true
                "custom" -> {
                    val code = current.dayOfWeek
                        .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH)
                        .uppercase()    // "MON", "TUE", etc.
                    frequencyDays.contains(code)
                }
                else -> false
            }

            if (isScheduled) {
                val dateKey = current.format(DateTimeFormatter.ISO_LOCAL_DATE)
                if (loggedSet.contains(dateKey)) {
                    streak++
                } else {
                    break  // Missed a scheduled day — streak is broken
                }
            }
            // Non-scheduled days are simply skipped

            current = current.minusDays(1)

            // Safety stop — don't walk back further than 365 days
            if (ChronoUnit.DAYS.between(current, asOf) > 365) break
        }

        return streak
    }

    // ── Fetch logs for progress blocks ────────────────────────────────────────

    /**
     * Fetches all logs for a habit within a date range.
     * Used by the GitHub-style progress block grid on the progress screen.
     *
     * @param from "YYYY-MM-DD" start of range (inclusive)
     * @param to   "YYYY-MM-DD" end of range (inclusive)
     */
    fun getLogs(
        habitId: String,
        from: String,
        to: String,
        onSuccess: (List<HabitLog>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("habits")
            .document(habitId)
            .collection("logs")
            .whereGreaterThanOrEqualTo("date", from)
            .whereLessThanOrEqualTo("date", to)
            .orderBy("date")
            .get()
            .addOnSuccessListener { snapshot ->
                val logs = snapshot.documents.mapNotNull { it.toObject(HabitLog::class.java) }
                onSuccess(logs)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch logs.")
            }
    }

    // ── Badge logic ───────────────────────────────────────────────────────────

    /**
     * Returns the badge earned for a given streak count.
     * Called in AchievementActivity after logHabit() succeeds.
     *
     * Returns null if no new badge is earned at this streak level.
     */
    fun getBadgeForStreak(streak: Int): Badge? {
        return when (streak) {
            7    -> Badge("🌱", "Seedling",    "7-day streak!")
            14   -> Badge("🌿", "Sprouting",   "2-week streak!")
            21   -> Badge("🌳", "Growing",     "21-day streak!")
            30   -> Badge("🔥", "On Fire",     "30-day streak!")
            60   -> Badge("⚡", "Electrified", "60-day streak!")
            100  -> Badge("💎", "Diamond",     "100-day streak!")
            365  -> Badge("👑", "Legend",      "1 year streak!")
            else -> null
        }
    }

    data class Badge(
        val emoji: String,
        val name: String,
        val description: String
    )
}