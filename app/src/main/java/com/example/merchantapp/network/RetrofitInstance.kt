// app/src/main/java/com/example/merchantapp/network/RetrofitInstance.kt
package com.example.merchantapp.network

import android.content.Context
import android.util.Log // <<<<<< ADD THIS IMPORT
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // --- THIS IS THE LINE WE ARE VERIFYING ---
    private const val BASE_URL = "https://stc.pinroj.com/"
    // --- END LINE TO VERIFY ---

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private var clientInstance: OkHttpClient? = null

    fun getOkHttpClient(context: Context): OkHttpClient {
        if (clientInstance == null) {
            val authInterceptor = AuthInterceptor(context.applicationContext)
            clientInstance = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .build()
        }
        return clientInstance!!
    }

    fun getApiService(context: Context): ApiService {
        // <<<<<< ADD LOGGING HERE >>>>>>
        Log.d("RetrofitInstance", "Attempting to create ApiService with BASE_URL: $BASE_URL")
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL) // This is where the check happens
            .client(getOkHttpClient(context.applicationContext))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        Log.d("RetrofitInstance", "Retrofit instance created successfully for BASE_URL: ${retrofit.baseUrl()}") // Log actual base URL used
        return retrofit.create(ApiService::class.java)
    }
}