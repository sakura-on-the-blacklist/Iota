package edu.ph.iota.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import edu.ph.iota.models.User
import edu.ph.iota.utilities.PreferenceManager
import java.lang.Exception

class HomeViewModel(
    application: Application
) : AndroidViewModel(
    application
) {

    val storage = FirebaseStorage.getInstance()

    val firestore = FirebaseFirestore.getInstance()

    val preferenceManager = PreferenceManager(application)

    private val _user = MutableLiveData(User())
    val user: LiveData<User> = _user

    init {
        onGetUser()
    }

    fun getPhoneNumber(): String? {
        return _user.value?.phoneNumber
    }

    fun getName(): String? {
        return _user.value?.name
    }

    fun getBirthday(): String? {
        return _user.value?.birthday
    }

    fun getUsername(): String? {
        return _user.value?.username
    }

    private fun onGetUser() {
        firestore
            .collection("users")
            .document(preferenceManager.getPhoneNumber()!!)
            .get()
            .addOnSuccessListener { onGetUserSuccess(it) }
            .addOnFailureListener { onGetUserFailure(it) }
    }

    private fun onGetUserSuccess(
        snapshot: DocumentSnapshot
    ) {
        if (snapshot.exists()) _user.value = snapshot.toObject(User::class.java)
    }

    private fun onGetUserFailure(
        exception: Exception
    ) {
        Log.d("Demo", "Error: ${exception.message}")
    }

}