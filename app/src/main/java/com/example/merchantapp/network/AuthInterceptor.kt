// File: (e.g.) app/src/main/java/com/example/merchantapp/network/AuthInterceptor.kt
package com.example.merchantapp.network // Or your chosen package

import android.content.Context
import com.example.merchantapp.data.local.AuthManager // Import your AuthManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the original request
        val originalRequest = chain.request()

        // Get the auth token using AuthManager
        // Use applicationContext to avoid potential memory leaks with Activity/Fragment context
        val token = AuthManager.getAuthToken(context.applicationContext)

        // If a token exists, add it to the Authorization header
        if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return chain.proceed(newRequest)
        }

        // If no token (user not logged in), proceed with the original request
        // Some endpoints (like login/register) don't need a token.
        return chain.proceed(originalRequest)
    }
}