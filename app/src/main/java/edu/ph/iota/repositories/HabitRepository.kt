package edu.ph.iota.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import edu.ph.iota.models.Habit

class HabitRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val habitsCollection get() = firestore.collection("habits")

    fun createHabit(habit: Habit): Task<Void> {
        val docRef = habitsCollection.document()
        val habitWithId = habit.copy(habitId = docRef.id)
        return docRef.set(habitWithId.toMap())
    }

    fun getHabits(userId: String): Task<QuerySnapshot> {
        // Removed orderBy to avoid requiring a composite index in Firestore
        return habitsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .get()
    }

    fun getHabitCountForIdentity(
        userId: String,
        identity: String,
        onResult: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        habitsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("identity", identity)
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { onResult(it.size()) }
            .addOnFailureListener { onError(it) }
    }

    fun updateHabit(habitId: String, fields: Map<String, Any?>): Task<Void> {
        return habitsCollection.document(habitId).update(fields)
    }

    fun archiveHabit(habitId: String): Task<Void> {
        return habitsCollection.document(habitId)
            .update("isActive", false)
    }
}
