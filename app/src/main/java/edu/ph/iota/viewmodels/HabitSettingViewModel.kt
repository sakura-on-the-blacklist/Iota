package edu.ph.iota.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.functions.FirebaseFunctions
import edu.ph.iota.models.Habit
import edu.ph.iota.repositories.HabitRepository
import edu.ph.iota.utilities.PreferenceManager
import edu.ph.iota.utilities.ReminderManager

class HabitSettingViewModel (application: Application) : AndroidViewModel(application)
{
    val habitRepository = HabitRepository()
    val preferenceManager = PreferenceManager(application)
    private val reminderManager = ReminderManager(application)
    private val functions = FirebaseFunctions.getInstance()

    private val _habitname     = MutableLiveData("")
    private val _location = MutableLiveData("")
    private val _identity = MutableLiveData(preferenceManager.getIdentity() ?: "")
    val identity: LiveData<String> = _identity

    fun setName(value: String)     {
        _habitname.value = value
        checkFormReady()
    }
    fun setLocation(value: String) {
        _location.value = value
        checkFormReady()
    }
    fun setIdentity(value: String) {
        if (preferenceManager.getIdentity() == null) {
            _identity.value = value
            checkFormReady()
        }
    }

    private val _frequency     = MutableLiveData("custom")
    private val _frequencyDays = MutableLiveData<List<String>>(emptyList())

    fun setFrequency(value: String)          {
        _frequency.value = value
        checkFormReady()
    }
    fun setFrequencyDays(days: List<String>) {
        _frequencyDays.value = days
        checkFormReady()
    }

    private val _goalValue = MutableLiveData(1.0)
    val goalValue: LiveData<Double> = _goalValue

    private val _goalUnit = MutableLiveData<List<String>>(emptyList())
    val goalUnit: LiveData<List<String>> = _goalUnit

    fun setGoalValue(value: Double) {
        _goalValue.value = value
        checkFormReady()
    }

    fun setGoalUnit(unit: String) {
        _goalUnit.value = listOf(unit)
        checkFormReady()
    }

    private val _startTime = MutableLiveData("")
    val startTime: LiveData<String> = _startTime

    fun setStartTime(value: String) {
        _startTime.value = value
        checkFormReady()
    }

    private val _reminderEnabled = MutableLiveData(false)
    val reminderEnabled: LiveData<Boolean> = _reminderEnabled

    private val _reminderTime = MutableLiveData("")
    val reminderTime: LiveData<String> = _reminderTime
    fun setReminderEnabled(enabled: Boolean) {
        _reminderEnabled.value = enabled
        if (!enabled) _reminderTime.value = ""
        checkFormReady()
    }

    fun setReminderTime(time: String) {
        _reminderTime.value = time
        checkFormReady()
    }

    sealed class AiState {
        object Idle : AiState()
        object Checking : AiState()
        data class Invalid(val reason: String, val suggestion: String) : AiState()
        data class Suggested(
            val frequencyDays: List<String>,
            val goalValue: Double,
            val goalUnit: String,
            val startTime: String,
            val reason: String
        ) : AiState()
        data class Error(val message: String) : AiState()
    }

    private val _aiState = MutableLiveData<AiState>(AiState.Idle)
    val aiState: LiveData<AiState> = _aiState

    fun analyzeHabit() {
        val habit = _habitname.value?.trim().orEmpty()
        val location = this._location.value?.trim()
        val identity = this.identity.value?.trim().orEmpty()

        if (habit.isBlank() || identity.isBlank()) {
            _aiState.value = AiState.Idle
            return
        }

        _aiState.value = AiState.Checking

        val data = hashMapOf(
            "habit" to habit,
            "identity" to identity,
            "location" to location
        )

        functions
            .getHttpsCallable("analyzeHabit")
            .call(data)
            .addOnSuccessListener { result ->
                val map = result.data as Map<*, *>
                val valid = map["valid"] as? Boolean ?: false
                val reason = map["reason"] as? String ?: ""
                val suggestion = map["suggestion"] as? String ?: ""

                if (!valid) {
                    _aiState.value = AiState.Invalid(reason, suggestion)
                    checkFormReady()
                } else {
                    val freqDays = (map["frequencyDays"] as? List<*>)?.map { it.toString() } ?: emptyList()
                    val gValue = (map["goalValue"] as? Number)?.toDouble() ?: 0.0
                    val gUnit = map["goalUnit"] as? String ?: ""
                    val sTime = map["startTime"] as? String ?: ""

                    _frequencyDays.value = freqDays
                    _frequency.value = if (freqDays.size == 7) "daily" else "custom"
                    _goalValue.value = gValue
                    _goalUnit.value = listOf(gUnit)
                    if (sTime.isNotBlank()) {
                        _startTime.value = sTime
                    }

                    _aiState.value = AiState.Suggested(
                        frequencyDays = freqDays,
                        goalValue = gValue,
                        goalUnit = gUnit,
                        startTime = sTime,
                        reason = reason
                    )
                    checkFormReady()
                }
            }
            .addOnFailureListener { e ->
                _aiState.value = AiState.Error(e.message ?: "Failed to analyze habit.")
                checkFormReady()
            }
    }

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

    fun saveHabit(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val error = validate()
        if (error != null) {
            onFailure(error)
            return
        }

        val userId = preferenceManager.getPhoneNumber() ?: ""
        val identity = _identity.value ?: ""

        _uiState.value = UiState.Loading

        habitRepository.getHabitCountForIdentity(
            userId = userId,
            identity = identity,
            onResult = { count ->
                if (count >= 6) {
                    _uiState.value = UiState.Error("You can only have 6 habits per identity.")
                    return@getHabitCountForIdentity
                }

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