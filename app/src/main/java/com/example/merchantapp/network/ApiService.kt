package com.example.merchantapp.network // Adjust if your root package is different

import com.example.merchantapp.data.LoginRequest // Import request data class
import com.example.merchantapp.data.LoginResponse // Import response data class
import retrofit2.Response // Import Retrofit's Response wrapper
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Defines the API endpoints using Retrofit annotations.
 */
interface ApiService {

    /**
     * Defines the login endpoint call.
     *
     * IMPORTANT: Replace "api/v1/auth/merchant/login" with the ACTUAL path
     *            to your merchant login endpoint on your backend API.
     *
     * - @POST specifies the HTTP method and the relative path to the base URL.
     * - @Body indicates that the 'request' object will be serialized into the request body (JSON).
     * - @Headers can be used to add static headers (like Content-Type) if needed.
     * - Response<LoginResponse>: Wraps the expected LoginResponse in Retrofit's Response class.
     *   This provides access to HTTP status codes and headers, useful for error handling.
     */
    @Headers("Content-Type: application/json") // Common header for sending JSON
    @POST("api/v1/auth/merchant/login") // <-- !!! REPLACE WITH YOUR ACTUAL LOGIN PATH !!!
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // TODO: Add other API endpoint definitions here later (e.g., get transactions, process transaction)

}