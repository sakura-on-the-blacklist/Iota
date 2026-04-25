package edu.ph.iota.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import edu.ph.iota.models.Habit

//Firestore read/write for /habits/{habitId}.

class HabitRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val habitsCollection get() = firestore.collection("habits")


    //PUT
    // Writes a new Habit document.
    fun createHabit(habit: Habit): Task<Void> {
        val docRef = habitsCollection.document()
        val habitWithId = habit.copy(habitId = docRef.id)
        return docRef.set(habitWithId.toMap())
    }


    //READ
    // Fetches all active habits for a user, ordered by displayOrder, drives the home screen pebbles.

    fun getHabits(userId: String): Task<QuerySnapshot> {
        return habitsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .orderBy("displayOrder")
            .get()
    }

    //Counts habits for a specific user + identity pair.
    //happens BEFORE calling createHabit() to enforce the 6-habit cap.


//        repo.getHabitCountForIdentity(userId, identity)
//            .addOnSuccessListener { count ->
//                if (count >= 6) showError("Max 6 habits per identity")
//                else proceedWithCreation()
//            }

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


//      EDIT
//      use later for edits from the habit settings page.
//
//      like: update reminder:
//        repo.updateHabit(habitId, mapOf(
//           "reminderEnabled" to true,
//           "reminderTime"    to "08:00"
//        ))

    fun updateHabit(habitId: String, fields: Map<String, Any?>): Task<Void> {
        return habitsCollection.document(habitId).update(fields)
    }



//     DELETE
//      Soft-deletes only by flipping isActive = false.
//      Logs and streaks are preserved for the progress screen.

    fun archiveHabit(habitId: String): Task<Void> {
        return habitsCollection.document(habitId)
            .update("isActive", false)
    }


}