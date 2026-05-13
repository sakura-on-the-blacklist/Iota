package edu.ph.iota.models

import com.google.firebase.Timestamp

data class Habit (
    val habitId: String = "",
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isActive: Boolean = true,
    //this is only 0-5, will be automatically assigned as (current habit count) when created.
    val displayOrder: Int = 0,

    //habit details (what i will do)
    val habitName: String = "",
    val identity: String = "",
    val location: String = "",

    //habit frequency (how often)
    val frequency: String = "custom",
    val frequencyDays: List<String> = emptyList(),

    //goal section
    val goalValue: Double = 1.0,
    val goalUnit: List<String> = emptyList(),

    //time section
    val startTime: String = "",

    //reminder section
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "",

    //streak
    // Computed from HabitLog subcollection, cached here for fast reads.
    // Written back by HabitLogRepository.logHabit() every time a log is created.

    /** Consecutive scheduled days logged without missing one. */
    val currentStreak: Int = 0,

    /** Highest streak ever reached for this habit. */
    val longestStreak: Int = 0,

    /** "YYYY-MM-DD" of the last logged day. Used to detect missed days. */
    val lastLoggedDate: String = "",

    /** Running total of all-time log entries for this habit. */
    val totalLogs: Int = 0

){
    fun toMap(): Map<String, Any?> = mapOf(
        "habitId"         to habitId,
        "userId"          to userId,
        "createdAt"       to createdAt,
        "isActive"        to isActive,
        "displayOrder"    to displayOrder,
        "habitName"       to habitName,
        "identity"        to identity,
        "location"        to location,
        "frequency"       to frequency,
        "frequencyDays"   to frequencyDays,
        "goalValue"       to goalValue,
        "goalUnit"        to goalUnit,
        "startTime"       to startTime,
        "reminderEnabled" to reminderEnabled,
        "reminderTime"    to reminderTime,
        "currentStreak"   to currentStreak,
        "longestStreak"   to longestStreak,
        "lastLoggedDate"  to lastLoggedDate,
        "totalLogs"       to totalLogs
    )
}