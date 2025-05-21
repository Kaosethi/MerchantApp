// File: app/src/main/java/com/example/merchantapp/network/AuthInterceptor.kt
package com.example.merchantapp.network

import android.content.Context
import android.util.Log // Import Log
import com.example.merchantapp.data.local.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath // Get path for logging
        Log.d("AuthInterceptor", "Intercepting request for path: $path")

        val token = AuthManager.getAuthToken(context.applicationContext)

        if (token != null) {
            // Log only a part of the token for security, or just its presence/absence
            Log.d("AuthInterceptor", "Token found for $path. Adding Authorization header. Token starts with: ${token.take(10)}...")
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return chain.proceed(newRequest)
        } else {
            Log.w("AuthInterceptor", "Token is NULL for path: $path. Proceeding without Authorization header.")
        }
        return chain.proceed(originalRequest)
    }
}