package edu.ph.iota.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.ph.iota.models.Habit
import edu.ph.iota.models.LocalAiResult
import edu.ph.iota.repositories.HabitRepository
import edu.ph.iota.utilities.PreferenceManager
import edu.ph.iota.utilities.ReminderManager


class HabitSettingViewModel (application: Application) : AndroidViewModel(application)
{
    val habitRepository = HabitRepository()

    val preferenceManager = PreferenceManager(application)
    private val reminderManager = ReminderManager(application)


    //identity setting section
    private val _habitname     = MutableLiveData("")
    private val _location = MutableLiveData("")
    private val _identity = MutableLiveData(preferenceManager.getIdentity() ?: "")
    val identity: LiveData<String> = _identity

    fun setName(value: String)     { _habitname.value = value }
    fun setLocation(value: String) { _location.value = value }
    fun setIdentity(value: String) {
        if (preferenceManager.getIdentity() == null) {
            _identity.value = value
        }
    }

    //frequency section
    private val _frequency     = MutableLiveData("custom")
    private val _frequencyDays = MutableLiveData<List<String>>(emptyList())

    fun setFrequency(value: String)          { _frequency.value = value }
    fun setFrequencyDays(days: List<String>) { _frequencyDays.value = days }

    //goal section
    private val _goalValue = MutableLiveData(1.0)
    val goalValue: LiveData<Double> = _goalValue

    private val _goalUnit = MutableLiveData<List<String>>(emptyList())
    val goalUnit: LiveData<List<String>> = _goalUnit

    fun setGoalValue(value: Double) {
        _goalValue.value = value
    }

    fun setGoalUnit(unit: String) {
        _goalUnit.value = listOf(unit)
    }

    //time section
    private val _startTime = MutableLiveData("")
    val startTime: LiveData<String> = _startTime

    fun setStartTime(value: String) {
        _startTime.value = value
    }

    //reminder section
    private val _reminderEnabled = MutableLiveData(false)
    val reminderEnabled: LiveData<Boolean> = _reminderEnabled

    private val _reminderTime = MutableLiveData("")
    val reminderTime: LiveData<String> = _reminderTime
    fun setReminderEnabled(enabled: Boolean) {
        _reminderEnabled.value = enabled
        if (!enabled) _reminderTime.value = "" // clear time when turned off
    }

    fun setReminderTime(time: String) {
        _reminderTime.value = time
    }

    //AI//
    sealed class AiState {
        /** No analysis yet — app just opened or fields are blank. */
        object Idle : AiState()

        /** Cloud Function call is in flight. Show spinner, block Continue. */
        object Checking : AiState()

        /**
         * Habit failed validation.
         * Continue is BLOCKED until the user fixes the habit name.
         */
        data class Invalid(val reason: String, val suggestion: String) : AiState()

        /**
         * Habit passed AND suggestions are ready.
         * HabitFragment reads these to auto-fill chips, goal, and time.
         * User can still override any auto-filled value.
         */
        data class Suggested(
            val frequencyDays: List<String>,
            val goalValue: Double,
            val goalUnit: String,
            val startTime: String,
            val reason: String
        ) : AiState()

        /** The Cloud Function call itself failed (network error, etc.) */
        data class Error(val message: String) : AiState()
    }

    private val _aiState = MutableLiveData<AiState>(AiState.Idle)
    val aiState: LiveData<AiState> = _aiState

    // ── AI analysis ───────────────────────────────────────────────────────────

    /**
     * Called by HabitFragment after a 1.5-second debounce whenever
     * habitName, location, or identity change.
     *
     * Requires: habit + identity both non-blank.
     * If either is blank → silently resets to Idle.
     */
    fun analyzeHabit() {
        val habit = _habitname.value?.trim().orEmpty()
        val location = this._location.value?.trim()
        val identity = this.identity.value?.trim().orEmpty()

        if (habit.isBlank() || identity.isBlank()) {
            _aiState.value = AiState.Idle
            return
        }

        _aiState.value = AiState.Checking

        val result = analyzeHabitLocally(habit, identity, location)

        if (!result.valid) {
            _aiState.value = AiState.Invalid(
                reason = result.reason,
                suggestion = result.suggestion
            )
            checkFormReady()
            return
        }

        // If valid, apply suggestions similar to how you handled Cloud Function result
        _frequencyDays.value = result.frequencyDays
        _frequency.value = if (result.frequencyDays.size == 7) "daily" else "custom"
        _goalValue.value = result.goalValue
        _goalUnit.value = listOf(result.goalUnit)
        if (result.startTime.isNotBlank()) {
            _startTime.value = result.startTime
        }

        _aiState.value = AiState.Suggested(
            frequencyDays = result.frequencyDays,
            goalValue = result.goalValue,
            goalUnit = result.goalUnit,
            startTime = result.startTime,
            reason = result.reason
        )

        checkFormReady()
    }

    fun analyzeHabitLocally(
        habit: String,
        identity: String,
        location: String?
    ): LocalAiResult {
        val trimmedHabit = habit.trim()
        val trimmedIdentity = identity.trim()
        val trimmedLocation = location?.trim().orEmpty()

        if (trimmedHabit.isBlank() || trimmedIdentity.isBlank()) {
            return LocalAiResult(
                valid = false,
                reason = "Please enter both your identity and a habit.",
                suggestion = "",
                frequencyDays = emptyList(),
                goalValue = 0.0,
                goalUnit = "",
                startTime = ""
            )
        }

        // 1) VAGUENESS CHECK
        val vaguePatterns = listOf(
            "go to the gym",
            "read books",
            "be healthy",
            "exercise more",
            "eat better",
            "study more",
            "work harder"
        )

        if (vaguePatterns.any { trimmedHabit.lowercase().contains(it) }) {
            return LocalAiResult(
                valid = false,
                reason = "The habit is too vague.",
                suggestion = "Make it concrete, e.g. \"do 20 push-ups\" or \"read 10 pages\".",
                frequencyDays = emptyList(),
                goalValue = 0.0,
                goalUnit = "",
                startTime = ""
            )
        }

        // 2) SPECIFICITY CHECK – very short or no number/unit
        val hasNumber = trimmedHabit.any { it.isDigit() }
        val hasTimeUnit = listOf("min", "minute", "hour", "hr", "km", "steps", "pages", "glasses", "reps")
            .any { trimmedHabit.lowercase().contains(it) }

        if ( !hasNumber && !hasTimeUnit) {
            return LocalAiResult(
                valid = false,
                reason = "The habit is not specific or measurable.",
                suggestion = "Include a clear amount or duration, e.g. \"walk 15 minutes\" or \"read 5 pages\".",
                frequencyDays = emptyList(),
                goalValue = 0.0,
                goalUnit = "",
                startTime = ""
            )
        }

        // 3) SUSTAINABILITY CHECK – very large numbers
        val tooHardPatterns = listOf(
            "10km", "10 km",
            "marathon",
            "2 hours", "3 hours", "4 hours"
        )

        if (tooHardPatterns.any { trimmedHabit.lowercase().contains(it) }) {
            return LocalAiResult(
                valid = false,
                reason = "The habit might be too hard to sustain every day.",
                suggestion = "Try a smaller, easier version that you can repeat consistently.",
                frequencyDays = emptyList(),
                goalValue = 0.0,
                goalUnit = "",
                startTime = ""
            )
        }

        // 4) RELEVANCE CHECK – crude keyword match with identity
//        val identityWords = trimmedIdentity.lowercase().split(" ").filter { it.length > 3 }
//        val habitWords = trimmedHabit.lowercase().split(" ")
//
//        val overlap = identityWords.any { w -> habitWords.any { it.contains(w) } }
//        if (!overlap) {
//            // If clearly non-related, flag it
//            return LocalAiResult(
//                valid = false,
//                reason = "The habit may not be directly related to your identity.",
//                suggestion = "Choose a habit that clearly moves you toward \"$identity\".",
//                frequencyDays = emptyList(),
//                goalValue = 0.0,
//                goalUnit = "",
//                startTime = ""
//            )
        //}

        // 5) If it passes all checks, mark as valid and auto-suggest schedule
        val defaultFrequency = listOf("MON","TUE","WED","THU","FRI","SAT","SUN")
        val (goalValue, goalUnit) = when {
            trimmedHabit.contains("walk", ignoreCase = true) ||
                    trimmedHabit.contains("run", ignoreCase = true) ->
                15.0 to "minutes"

            trimmedHabit.contains("read", ignoreCase = true) ->
                10.0 to "pages"

            trimmedHabit.contains("push-up", ignoreCase = true) ||
                    trimmedHabit.contains("push up", ignoreCase = true) ->
                10.0 to "reps"

            else -> 1.0 to "times"
        }

        val startTime = if (trimmedLocation.lowercase().contains("morning")) {
            "07:00"
        } else if (trimmedLocation.lowercase().contains("evening") ||
            trimmedLocation.lowercase().contains("night")) {
            "20:00"
        } else {
            "" // let user set it
        }

        return LocalAiResult(
            valid = true,
            reason = "This habit looks relevant, specific, and sustainable.",
            suggestion = "",
            frequencyDays = defaultFrequency,
            goalValue = goalValue,
            goalUnit = goalUnit,
            startTime = startTime
        )
    }

    //UI
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Error(val message: String) : UiState()
        object Success : UiState()
    }

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    private val _isFormReady = MutableLiveData(false)
    val isFormReady: LiveData<Boolean> = _isFormReady

    fun checkFormReady(){
        _isFormReady.value = validate() == null
    }

    //validation
    fun validate(): String? {
        if (_habitname.value.isNullOrBlank())
            return "Please enter a habit name."
        if (_location.value.isNullOrBlank())
            return "Deciding where to do it is important."
        if (_identity.value.isNullOrBlank())
            return "Please enter your identity statement."
        if ((_goalValue.value ?: 0.0) <= 0.0)
            return "Goal must be greater than zero."
        if (_frequencyDays.value.isNullOrEmpty())
            return "Please select at least one day for your custom schedule."
        if (_reminderEnabled.value == true && _reminderTime.value.isNullOrBlank())
            return "Please set a reminder time."
        return null
    }

    //gets triggered after continue button is pressed
    fun saveHabit(onSuccess: () -> Unit, onFailure: (String) -> Unit) {

        val error = validate()
        if (error != null) {
            onFailure(error)
            return
        }

        val userId = preferenceManager.getPhoneNumber() ?: ""
        val identity = _identity.value ?: ""

        _uiState.value = UiState.Loading

        //enforce 6 habit per identity
        habitRepository.getHabitCountForIdentity(
            userId = userId,
            identity = identity,
            onResult = { count ->
                if (count >= 6) {
                    _uiState.value = UiState.Error("You can only have 6 habits per identity.")

                    return@getHabitCountForIdentity
                }

                // Save identity to preferences if not already set
                if (preferenceManager.getIdentity() == null) {
                    preferenceManager.setIdentity(identity)
                }

                writeHabit(userId, count, onSuccess, onFailure)
            },
            onError = { e ->
                _uiState.value = UiState.Error(e.message ?: "Failed to check habit count.")

            }
        )
    }

    private fun writeHabit(
        userId: String,
        currentCount: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Generate ID beforehand so we can use it for reminders
        val newHabitId = habitRepository.habitsCollection.document().id

        val habit = Habit(
            habitId = newHabitId,
            userId = userId,
            habitName = _habitname.value ?: "",
            identity = _identity.value ?: "",
            location = _location.value ?: "",
            frequency = _frequency.value ?: "custom",
            frequencyDays = _frequencyDays.value ?: emptyList(),
            goalValue = _goalValue.value ?: 1.0,
            goalUnit = _goalUnit.value ?: emptyList(),
            startTime = _startTime.value ?: "anytime",
            reminderEnabled = _reminderEnabled.value ?: false,
            reminderTime = _reminderTime.value ?: "",
            displayOrder = currentCount
        )

        habitRepository.createHabit(habit)
            .addOnSuccessListener {

                if (habit.reminderEnabled) {
                    reminderManager.scheduleReminder(habit)
                }

                _uiState.value = UiState.Success
                onSuccess()
            }
            .addOnFailureListener { e ->
                val msg = e.message ?: "Failed to save habit."
                _uiState.value = UiState.Error(msg)
                onFailure(msg)
            }
    }


}
