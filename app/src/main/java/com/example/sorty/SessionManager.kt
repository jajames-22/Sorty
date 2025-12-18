package com.example.sorty

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    companion object {
        const val PREF_NAME = "SortyUserSession"
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
        const val KEY_EMAIL = "email" // 1. Add this key
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    fun setLogin(isLoggedIn: Boolean) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }

    // 2. ADD THIS FUNCTION TO FIX THE ERROR
    fun createLoginSession(email: String) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_EMAIL, email) // Save the email
        editor.apply()
    }

    // Optional: You can use this later to get the current user's email
    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    // Add this inside SessionManager class
    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        editor.putBoolean("IS_FIRST_TIME", isFirstTime)
        editor.apply()
    }

    fun isFirstTimeLaunch(): Boolean {
        return prefs.getBoolean("IS_FIRST_TIME", true)
    }
}