package edu.ph.iota.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import edu.ph.iota.models.Reflection

class ReflectionRepository @JvmOverloads constructor(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val reflectionsCollection get() = firestore.collection("reflections")

    fun getReflectionsByHabitId(habitId: String): Task<QuerySnapshot> {
        return reflectionsCollection
            .whereEqualTo("habitId", habitId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
    }

    fun getReflectionsByUserId(userId: String): Task<QuerySnapshot> {
        return reflectionsCollection
            .whereEqualTo("userId", userId)
//            .orderBy("date", Query.Direction.DESCENDING)
            .get()
    }

    fun createReflection(reflection: Reflection): Task<Void> {
        val docRef = reflectionsCollection.document()
        val reflectionWithId = reflection.copy(reflectionId = docRef.id)
        return docRef.set(reflectionWithId.toMap())
    }

    fun deleteReflection(reflectionId: String): Task<Void> {
        return reflectionsCollection.document(reflectionId).delete()
    }

    fun updateReflection(reflectionId: String, content: String): Task<Void> {
        return reflectionsCollection.document(reflectionId)
            .update(mapOf("content" to content))
    }
}