// File: app/src/main/java/com/example/merchantapp/network/ApiService.kt
package com.example.merchantapp.network

import com.example.merchantapp.model.LoginRequest
import com.example.merchantapp.model.LoginResponse
import com.example.merchantapp.model.MerchantProfileResponse
import com.example.merchantapp.model.TransactionApiResponse
import com.example.merchantapp.model.MerchantRegistrationRequest // <<< ADD THIS IMPORT
import com.example.merchantapp.model.MerchantRegistrationResponse // <<< ADD THIS IMPORT

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @POST("api/merchant-app/auth/login")
    suspend fun merchantLogin(@Body loginRequest: LoginRequest): Response<LoginResponse>

    // --- ADD THIS FUNCTION ---
    @POST("api/merchant-app/auth/register")
    suspend fun merchantRegister(@Body registrationRequest: MerchantRegistrationRequest): Response<MerchantRegistrationResponse>
    // --- END ---

    @GET("api/merchant-app/profile")
    @Headers("Authorization: Bearer mock-jwt-token-for-merchant") // TEMPORARY
    suspend fun getMerchantProfile(): Response<MerchantProfileResponse>

    @GET("api/merchant-app/transactions")
    @Headers("Authorization: Bearer mock-jwt-token-for-merchant") // TEMPORARY
    suspend fun getMerchantTransactions(): Response<List<TransactionApiResponse>>

    /*
    // TODO: Define other endpoints for beneficiaries, processing transactions, etc.
    */
}