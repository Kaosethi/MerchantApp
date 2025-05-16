// MODIFIED: app/src/main/java/com/example/merchantapp/network/ApiService.kt
package com.example.merchantapp.network

// --- ADDED: Import statements for all your model classes ---
import com.example.merchantapp.model.LoginRequest
import com.example.merchantapp.model.LoginResponse
import com.example.merchantapp.model.MerchantProfileResponse
import com.example.merchantapp.model.MerchantRegistrationRequest
import com.example.merchantapp.model.MerchantRegistrationResponse
import com.example.merchantapp.model.TransactionApiResponse
// --- End Added Imports ---

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
// import retrofit2.http.Headers // Only if you use it for non-auth headers elsewhere
import retrofit2.http.POST

interface ApiService {

    @POST("api/merchant-app/auth/login")
    suspend fun merchantLogin(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/merchant-app/auth/register")
    suspend fun merchantRegister(@Body registrationRequest: MerchantRegistrationRequest): Response<MerchantRegistrationResponse>

    @GET("api/merchant-app/profile")
    suspend fun getMerchantProfile(): Response<MerchantProfileResponse>

    @GET("api/merchant-app/transactions")
    suspend fun getMerchantTransactions(): Response<List<TransactionApiResponse>>

    /*
    // TODO: Define other endpoints
    */
}