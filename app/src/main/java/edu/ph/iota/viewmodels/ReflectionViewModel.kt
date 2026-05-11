package edu.ph.iota.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import edu.ph.iota.models.Reflection
import edu.ph.iota.repositories.ReflectionRepository
import edu.ph.iota.utilities.PreferenceManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReflectionViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val reflectionRepository = ReflectionRepository(firestore)
    private val preferenceManager = PreferenceManager(application)

    private val _reflections = MutableLiveData<List<Reflection>>(emptyList())
    val reflections: LiveData<List<Reflection>> = _reflections

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadAllReflections() {
        val userId = preferenceManager.getUserId() ?: preferenceManager.getPhoneNumber()

        if (userId.isNullOrBlank()) {
            _errorMessage.value = "User not found"
            return
        }

        _isLoading.value = true

        reflectionRepository.getReflectionsByUserId(userId)
            .addOnSuccessListener { snapshot ->
                _isLoading.value = false
                val reflections = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Reflection::class.java)?.copy(reflectionId = doc.id)
                }
                _reflections.value = reflections
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                Log.e("ReflectionViewModel", "Load failed: ${e.message}")
                _errorMessage.value = "Failed to load reflections: ${e.message}"
            }
    }

    fun loadReflectionsByHabitId(habitId: String) {
        _isLoading.value = true

        reflectionRepository.getReflectionsByHabitId(habitId)
            .addOnSuccessListener { snapshot ->
                _isLoading.value = false
                val reflections = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Reflection::class.java)?.copy(reflectionId = doc.id)
                }
                _reflections.value = reflections
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                Log.e("ReflectionViewModel", "Load by habit failed: ${e.message}")
                _errorMessage.value = "Failed to load reflections for this habit"
            }
    }

    fun saveReflection(content: String, habitId: String? = null) {
        val userId = preferenceManager.getUserId() ?: preferenceManager.getPhoneNumber()

        if (userId.isNullOrBlank()) {
            _errorMessage.value = "User not found"
            return
        }

        val now = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

        val reflection = Reflection(
            habitId = habitId ?: "",
            userId = userId,
            content = content,
            date = dateFormat.format(now),
            displayDate = displayDateFormat.format(now),
            timestamp = now.time
        )

        reflectionRepository.createReflection(reflection)
            .addOnSuccessListener {
                loadAllReflections() // Refresh the list
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to save reflection: ${e.message}"
            }
    }

    fun deleteReflection(reflectionId: String) {
        reflectionRepository.deleteReflection(reflectionId)
            .addOnSuccessListener {
                loadAllReflections() // Refresh the list
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to delete reflection: ${e.message}"
            }
    }
}