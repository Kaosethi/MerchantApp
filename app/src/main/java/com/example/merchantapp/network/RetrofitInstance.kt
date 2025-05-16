// MODIFIED: app/src/main/java/com/example/merchantapp/network/RetrofitInstance.kt
package com.example.merchantapp.network

import android.content.Context // ADDED
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
// AuthInterceptor import will be here if in the same package, or specify full path

object RetrofitInstance {

    private const val BASE_URL = "https://stc.pinroj.com/" // Ensure trailing slash!

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Store the client instance once created
    private var clientInstance: OkHttpClient? = null

    // Function to get or create the OkHttpClient, now requires context
    fun getOkHttpClient(context: Context): OkHttpClient {
        if (clientInstance == null) {
            // Create AuthInterceptor, passing the application context
            val authInterceptor = AuthInterceptor(context.applicationContext)

            clientInstance = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor) // ADDED: AuthInterceptor
                // .connectTimeout(30, TimeUnit.SECONDS)
                // .readTimeout(30, TimeUnit.SECONDS)
                .build()
        }
        return clientInstance!!
    }

    // The Retrofit instance will now need the client which needs context
    // This means 'api' can't be a simple 'by lazy' if it depends on context being passed.
    // One way is to make 'api' a function too, or initialize RetrofitInstance elsewhere.

    // Let's make 'api' a function that takes context
    fun getApiService(context: Context): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient(context.applicationContext)) // Use the OkHttpClient with context
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    // If you prefer 'api' as a property, you'd need to initialize RetrofitInstance
    // with context from your Application class.
    // Example (more complex to set up initially):
    // private lateinit var applicationContext: Context
    // fun init(context: Context) {
    // this.applicationContext = context.applicationContext
    // }
    // val api: ApiService by lazy {
    // if (!::applicationContext.isInitialized) {
    // throw IllegalStateException("RetrofitInstance has not been initialized. Call init() first.")
    // }
    // createRetrofit(applicationContext).create(ApiService::class.java)
    // }
}