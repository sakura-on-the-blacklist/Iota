package edu.ph.iota.utilities

import android.content.Context
import androidx.core.content.edit

class PreferenceManager(
    context: Context
) {

    private val preferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    fun onLogout() {
        preferences.edit {
            clear()
        }
    }

    fun setPhoneNumber(
        phoneNumber: String
    ) {
        preferences.edit {
            putString("phoneNumber", phoneNumber)
        }
    }

    fun getPhoneNumber(): String? {
        return preferences.getString("phoneNumber", null)
    }

}