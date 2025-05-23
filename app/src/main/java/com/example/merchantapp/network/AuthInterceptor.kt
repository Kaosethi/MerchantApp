package com.example.merchantapp.network

import android.content.Context
import android.util.Log
import com.example.merchantapp.auth.AuthEvent
import com.example.merchantapp.auth.AuthEventBus
import com.example.merchantapp.data.local.AuthManager
import kotlinx.coroutines.runBlocking // <<< NEW IMPORT
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        Log.d("AuthInterceptor", "Intercepting request for path: $path")

        val token = AuthManager.getAuthToken(context.applicationContext)

        val requestToSend = if (token != null) {
            Log.d("AuthInterceptor", "Token found for $path. Adding Authorization header. Token starts with: ${token.take(10)}...")
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            Log.w("AuthInterceptor", "Token is NULL for path: $path. Proceeding without Authorization header.")
            originalRequest
        }

        val response: Response
        try {
            response = chain.proceed(requestToSend)
        } catch (e: IOException) {
            Log.e("AuthInterceptor", "Network IOException for path $path: ${e.message}", e)
            throw e // Re-throw network exceptions
        }

        // Check for 401 Unauthorized response
        if (response.code == 401) {
            Log.w("AuthInterceptor", "Received 401 Unauthorized for path: $path. Token might be expired or invalid.")

            // Avoid logout loop if the login attempt itself fails with 401 (e.g. bad credentials)
            // The login API should ideally return more specific errors than a generic 401 for bad creds.
            // Check if the current path is NOT the login path.
            // Adjust "/api/merchant-app/auth/login" if your actual login path is different.
            if (!path.contains("/api/merchant-app/auth/login", ignoreCase = true)) {
                Log.d("AuthInterceptor", "401 was not on login path. Logging out and posting event.")
                AuthManager.logout(context.applicationContext) // Clear stored token and user data

                // Post an event to be observed by the UI layer to trigger navigation
                // runBlocking is used here because Interceptor.intercept is not a suspend function.
                // This is generally acceptable for one-shot events like this from a background thread.
                runBlocking {
                    AuthEventBus.postEvent(AuthEvent.TokenExpiredOrInvalid)
                }
                Log.d("AuthInterceptor", "Logged out and posted TokenExpiredOrInvalid event.")
            } else {
                Log.d("AuthInterceptor", "401 on login path, not triggering global logout from interceptor.")
            }
        }
        return response
    }
}