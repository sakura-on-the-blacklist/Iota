package edu.ph.iota.viewmodels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import edu.ph.iota.models.Habit
import edu.ph.iota.models.User
import edu.ph.iota.repositories.HabitRepository
import edu.ph.iota.utilities.PreferenceManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    val firestore          = FirebaseFirestore.getInstance()
    val preferenceManager  = PreferenceManager(application)
    val habitRepository    = HabitRepository(firestore)

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _habitsForDay = MutableLiveData<List<Habit>>()
    val habitsForDay: LiveData<List<Habit>> = _habitsForDay

    private var allHabits: List<Habit> = emptyList()



    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage


    init {
        loadUser()
    }

    //load user

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadUser() {

        val userId = preferenceManager.getUserId()

        if (!userId.isNullOrBlank()) {
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val u = snapshot.toObject(User::class.java)
                        if (u != null) {
                            _user.value = u
                            loadHabits(userId)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeViewModel", "loadUser by id failed: ${e.message}")
                    _errorMessage.value = "Failed to load user."
                }

        } else {
            val phone = preferenceManager.getPhoneNumber() ?: return
            firestore.collection("users")
                .whereEqualTo("phoneNumber", phone)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    val doc = snapshot.documents.firstOrNull() ?: return@addOnSuccessListener
                    val u = doc.toObject(User::class.java)
                    if (u != null) {
                        _user.value = u
                        preferenceManager.setUserId(doc.id)
                        loadHabits(doc.id)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeViewModel", "loadUser by phone failed: ${e.message}")
                    _errorMessage.value = "Failed to load user."
                }
        }
    }

//habit loading
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadHabits(userId: String) {
        _isLoading.value = true

        habitRepository.getHabits(userId)
            .addOnSuccessListener { snapshot ->
                _isLoading.value = false
                allHabits = snapshot.documents.mapNotNull { it.toObject(Habit::class.java) }
                filterHabitsForDate(LocalDate.now())
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                Log.e("HomeViewModel", "loadHabits failed: ${e.message}")
                _errorMessage.value = "Failed to load habits."
            }
    }

    //day filtering

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterHabitsForDate(date: LocalDate) {
        val dayCode = date.dayOfWeek.toShortCode()

        val filtered = allHabits.filter { habit ->
            when (habit.frequency) {
                "daily"  -> true
                "custom" -> habit.frequencyDays.contains(dayCode)
                else     -> false
            }
        }

        _habitsForDay.value = filtered
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun DayOfWeek.toShortCode(): String =
        this.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase()

    fun getUsername(): String? = _user.value?.username
    fun getName(): String?     = _user.value?.name
}