package edu.ph.iota.viewmodels

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import edu.ph.iota.models.User
import edu.ph.iota.utilities.PreferenceManager

class MainViewModel(
    application: Application
) : AndroidViewModel(
    application
) {

    val firestore = FirebaseFirestore.getInstance()

    val preferenceManager = PreferenceManager(application)

    private val _user = MutableLiveData(User())
    val user: LiveData<User> = _user

    init {}

    fun setUser(
        user: User
    ) {
        _user.value = user
    }

    fun getUser(): User? {
        return _user.value
    }

    fun setPhoneNumber(
        phoneNumber: String
    ) {
        _user.value = _user.value?.copy(phoneNumber = phoneNumber)
    }

    fun getPhoneNumber(): String? {
        return _user.value?.phoneNumber
    }

    fun setName(
        name: String
    ) {
        _user.value = _user.value?.copy(name = name)
    }

    fun getName(): String? {
        return _user.value?.name
    }

    fun setBirthday(
        birthday: String
    ) {
        _user.value = _user.value?.copy(birthday = birthday)
    }

    fun getBirthday(): String? {
        return _user.value?.birthday
    }

    fun setUsername(
        username: String
    ) {
        _user.value = _user.value?.copy(username = username)
    }

    fun getUsername(): String? {
        return _user.value?.username
    }

}