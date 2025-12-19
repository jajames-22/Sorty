package com.example.sorty

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    companion object {
        private const val PREF_NAME = "SortyUserSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_EMAIL = "email"
        private const val KEY_FIRST_NAME = "firstName" // Added for greeting
        private const val KEY_IS_FIRST_TIME = "isFirstTime"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    /**
     * Creates a full login session with email and name
     */
    fun createLoginSession(email: String, firstName: String) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_FIRST_NAME, firstName)
        editor.apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    /**
     * Retrieves the stored first name for the "Hello, (Name)" greeting
     */
    fun getFirstName(): String {
        return prefs.getString(KEY_FIRST_NAME, "User") ?: "User"
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }

    // --- First Launch Logic ---
    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        editor.putBoolean(KEY_IS_FIRST_TIME, isFirstTime)
        editor.apply()
    }


    fun isFirstTimeLaunch(): Boolean = prefs.getBoolean(KEY_IS_FIRST_TIME, true)
}