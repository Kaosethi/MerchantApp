// MODIFIED: app/src/main/java/com/example/merchantapp/data/local/AuthManager.kt
package com.example.merchantapp.data.local

import android.content.Context
import android.content.SharedPreferences

object AuthManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in" // Can keep if still useful for quick checks
    private const val KEY_USER_EMAIL = "user_email"     // Can keep for display purposes
    private const val KEY_AUTH_TOKEN = "auth_token"     // NEW: For storing JWT

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean, email: String? = null, token: String? = null) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        if (isLoggedIn) {
            email?.let { editor.putString(KEY_USER_EMAIL, it) }
            token?.let { editor.putString(KEY_AUTH_TOKEN, it) } // Store the token
        } else {
            editor.remove(KEY_USER_EMAIL)
            editor.remove(KEY_AUTH_TOKEN) // Clear the token on logout
        }
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserEmail(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_EMAIL, null)
    }

    fun getAuthToken(context: Context): String? { // NEW: Getter for the token
        return getPreferences(context).getString(KEY_AUTH_TOKEN, null)
    }

    fun logout(context: Context) {
        setLoggedIn(context, false) // This will clear email and token as per setLoggedIn logic
    }
}