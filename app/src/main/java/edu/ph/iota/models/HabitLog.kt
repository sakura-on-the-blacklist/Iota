package edu.ph.iota.models


import com.google.firebase.Timestamp

/**
 * HabitLog — stored at /habits/{habitId}/logs/{logId}
 *
 * One document is created each time the user logs a habit
 * (after holding the pebble for 3 seconds).
 *
 * The [date] field ("YYYY-MM-DD") is the primary key used for:
 *   - Checking if a habit was already logged today
 *   - Calculating streaks (consecutive logged dates)
 *   - Populating the GitHub-style progress blocks
 *   - Allowing retroactive edits (up to 7 days back)
 */
data class HabitLog(
    val logId: String = "",
    val habitId: String = "",

    /**
     * "YYYY-MM-DD" string — e.g. "2025-04-30"
     * Stored as a plain string (not Timestamp) so it's trivial
     * to query a date range and sort lexicographically.
     */
    val date: String = "",

    /**
     * How much the user did. E.g. 3.0 for "3 times", 5.2 for "5.2 km".
     * Compared against Habit.goalValue to determine completion.
     */
    val value: Double = 0.0,

    /** True when value >= Habit.goalValue (goal was fully met). */
    val completed: Boolean = false,

    val loggedAt: Timestamp = Timestamp.now(),

    /**
     * Set when the user edits a past log.
     * Null on original logs. Used to audit retroactive changes.
     */
    val editedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "logId"     to logId,
        "habitId"   to habitId,
        "date"      to date,
        "value"     to value,
        "completed" to completed,
        "loggedAt"  to loggedAt,
        "editedAt"  to editedAt
    )
}
