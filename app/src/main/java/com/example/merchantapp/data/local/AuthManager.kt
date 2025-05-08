// AuthManager.kt
package com.example.merchantapp.data.local // Or com.example.merchantapp.util

import android.content.Context
import android.content.SharedPreferences

// ADDED: Entire new file
object AuthManager {
    private const val PREFS_NAME = "MerchantAppAuthPrefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_LOGGED_IN_MERCHANT_EMAIL = "loggedInMerchantEmail" // Optional
    private const val KEY_LAST_LOGIN_TIMESTAMP = "lastLoginTimestamp"

    // Define a timeout period, e.g., 24 hours in milliseconds
    private const val LOGIN_TIMEOUT_MS = 24 * 60 * 60 * 1000L // 24 hours

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean, email: String? = null) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        if (isLoggedIn) {
            editor.putLong(KEY_LAST_LOGIN_TIMESTAMP, System.currentTimeMillis())
            email?.let { editor.putString(KEY_LOGGED_IN_MERCHANT_EMAIL, it) }
        } else {
            editor.remove(KEY_LAST_LOGIN_TIMESTAMP)
            editor.remove(KEY_LOGGED_IN_MERCHANT_EMAIL)
        }
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = getPreferences(context)
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!loggedIn) {
            return false
        }

        // Check for timeout
        val lastLoginTime = prefs.getLong(KEY_LAST_LOGIN_TIMESTAMP, 0L)
        if (System.currentTimeMillis() - lastLoginTime > LOGIN_TIMEOUT_MS) {
            // Timeout exceeded, log out user
            setLoggedIn(context, false) // Clear login state
            return false
        }
        return true
    }

    fun getLoggedInMerchantEmail(context: Context): String? {
        return getPreferences(context).getString(KEY_LOGGED_IN_MERCHANT_EMAIL, null)
    }

    fun clearLoginData(context: Context) {
        setLoggedIn(context, false) // This already clears all relevant keys
    }
}