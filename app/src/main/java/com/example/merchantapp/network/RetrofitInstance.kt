// File: app/src/main/java/com/example/merchantapp/network/RetrofitInstance.kt
package com.example.merchantapp.network

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://stc.pinroj.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Logs request and response bodies
    }

    // Volatile to ensure visibility across threads for the ApiService instance
    @Volatile private var apiServiceInstance: ApiService? = null
    // OkHttpClient can also be a singleton instance
    @Volatile private var okHttpClientInstance: OkHttpClient? = null

    // Private function to get or create the OkHttpClient instance
    private fun getOkHttpClient(context: Context): OkHttpClient {
        // Double-checked locking for thread safety, though for an object singleton it might be simpler
        return okHttpClientInstance ?: synchronized(this) {
            okHttpClientInstance ?: run {
                // Use application context to avoid memory leaks from short-lived contexts like Activities
                val appContext = context.applicationContext
                val authInterceptor = AuthInterceptor(appContext) // Your existing AuthInterceptor

                Log.d("RetrofitInstance", "Creating new OkHttpClient instance.")
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor) // For logging HTTP traffic
                    .addInterceptor(authInterceptor)    // For adding JWT tokens
                    .build().also { okHttpClientInstance = it }
            }
        }
    }

    // Public function to get the ApiService instance (singleton)
    fun getApiService(context: Context): ApiService {
        // Double-checked locking for thread safety
        return apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: run {
                Log.d("RetrofitInstance", "Creating new ApiService instance with BASE_URL: $BASE_URL")
                // Use application context when getting OkHttpClient
                val appContext = context.applicationContext
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getOkHttpClient(appContext)) // Use our configured OkHttpClient
                    .addConverterFactory(GsonConverterFactory.create()) // For JSON serialization/deserialization
                    .build()

                Log.d("RetrofitInstance", "Retrofit instance created successfully. Base URL: ${retrofit.baseUrl()}")
                retrofit.create(ApiService::class.java).also { apiServiceInstance = it }
            }
        }
    }
}