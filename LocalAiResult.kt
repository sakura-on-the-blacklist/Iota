package edu.ph.iota.models

data class LocalAiResult(
    val valid: Boolean,
    val reason: String,
    val suggestion: String,
    val frequencyDays: List<String>,
    val goalValue: Double,
    val goalUnit: String,
    val startTime: String
)