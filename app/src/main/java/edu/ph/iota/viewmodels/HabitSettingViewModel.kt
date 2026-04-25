package edu.ph.iota.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.ph.iota.models.Habit
import edu.ph.iota.repositories.HabitRepository
import edu.ph.iota.utilities.PreferenceManager

class HabitSettingViewModel (application: Application) : AndroidViewModel(application)
{
    val habitRepository = HabitRepository()

    val preferenceManager = PreferenceManager(application)

    //identity setting section
    private val _habitname     = MutableLiveData("")
    private val _location = MutableLiveData("")
    private val _identity = MutableLiveData("")

    fun setName(value: String)     { _habitname.value = value }
    fun setLocation(value: String) { _location.value = value }
    fun setIdentity(value: String) { _identity.value = value }

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

    //validation (did the user input everything correctly?
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
                    onFailure("You can only have 6 habits per identity.")
                    return@getHabitCountForIdentity
                }
                writeHabit(userId, count, onSuccess, onFailure)
            },
            onError = { e ->
                _uiState.value = UiState.Error(e.message ?: "Failed to check habit count.")
                onFailure(e.message ?: "Failed to check habit count.")
            }
        )
    }

    private fun writeHabit(
        userId: String,
        currentCount: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val habit = Habit(
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
                _uiState.value = UiState.Success
                onSuccess()
            }
            .addOnFailureListener { e ->
                _uiState.value = UiState.Error(e.message ?: "Failed to save habit.")
                onFailure(e.message ?: "Failed to save habit.")
            }
    }


}


