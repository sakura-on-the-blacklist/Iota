package edu.ph.iota.models

data class Reflection(
    val reflectionId: String = "",      // Firestore document ID
    val habitId: String = "",           // Which habit this reflection belongs to
    val userId: String = "",            // User who wrote the reflection
    val content: String = "",           // The reflection text
    val date: String = "",              // Date of the reflection (YYYY-MM-DD)
    val displayDate: String = "",       // Formatted date for display (April 4, 2026)
    val timestamp: Long = 0L            // Unix timestamp for sorting
) {
    fun toMap(): Map<String, Any> = mapOf(
        "habitId" to habitId,
        "userId" to userId,
        "content" to content,
        "date" to date,
        "displayDate" to displayDate,
        "timestamp" to timestamp
    )
}