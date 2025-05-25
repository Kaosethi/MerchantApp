// MODIFIED: app/src/main/java/com/example/merchantapp/data/local/AuthManager.kt
package com.example.merchantapp.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log // Import Log

object AuthManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_AUTH_TOKEN = "auth_token"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean, email: String? = null, token: String? = null) {
        Log.d("AuthManager", "setLoggedIn called. IsLoggedIn: $isLoggedIn, Email: $email, Token provided starts with: ${token?.take(10)}...")
        val editor = getPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        if (isLoggedIn) {
            email?.let { editor.putString(KEY_USER_EMAIL, it) }
            // Only store the token if it's actually provided and we are logging in
            if (token != null) {
                editor.putString(KEY_AUTH_TOKEN, token)
                Log.d("AuthManager", "Token STORED. Starts with: ${token.take(10)}...")
            } else {
                // If logging in but no token provided, it's an issue.
                // Or, if this is just updating isLoggedIn state without touching token,
                // then this branch might not be an error.
                // For clarity, if token is meant to be set, it should be non-null.
                Log.w("AuthManager", "setLoggedIn called with isLoggedIn=true but token is NULL. Token not stored/updated.")
            }
        } else {
            // Logging out, remove sensitive data
            editor.remove(KEY_USER_EMAIL)
            editor.remove(KEY_AUTH_TOKEN)
            Log.d("AuthManager", "Logged out. Email and Token REMOVED from SharedPreferences.")
        }
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val loggedInStatus = getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
        // Log.v("AuthManager", "isLoggedIn check returning: $loggedInStatus") // Optional: very verbose
        return loggedInStatus
    }

    fun getUserEmail(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_EMAIL, null)
    }

    fun getAuthToken(context: Context): String? {
        val token = getPreferences(context).getString(KEY_AUTH_TOKEN, null)
        if (token != null) {
            Log.d("AuthManager", "getAuthToken RETRIEVED. Token starts with: ${token.take(10)}...")
        } else {
            Log.w("AuthManager", "getAuthToken RETRIEVED NULL token.")
        }
        return token
    }

    fun logout(context: Context) {
        Log.d("AuthManager", "logout called.")
        // setLoggedIn(false) will handle clearing the token and email
        setLoggedIn(context, isLoggedIn = false, email = null, token = null)
    }
}