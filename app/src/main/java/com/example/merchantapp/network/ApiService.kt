package com.example.merchantapp.network

import com.example.merchantapp.data.ProcessTransactionRequest
import com.example.merchantapp.data.ProcessTransactionResponse
import com.example.merchantapp.data.QrPayload
// Import the data classes for auth and profile
import com.example.merchantapp.model.LoginRequest
import com.example.merchantapp.model.LoginResponse
import com.example.merchantapp.model.MerchantProfileResponse
import com.example.merchantapp.model.MerchantRegistrationRequest
import com.example.merchantapp.model.MerchantRegistrationResponse
import com.example.merchantapp.model.TransactionHistoryApiResponse
// Assuming ValidatedBeneficiary is now in the model package
import com.example.merchantapp.model.ValidatedBeneficiary
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // --- Merchant Auth Endpoints ---
    @POST("api/merchant-app/auth/register") // Example Path - Adjust if different
    suspend fun registerMerchant(
        @Body request: MerchantRegistrationRequest
    ): Response<MerchantRegistrationResponse>

    @POST("api/merchant-app/auth/login") // Example Path - Adjust if different
    suspend fun loginMerchant(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/merchant-app/auth/profile") // Example Path - Adjust if different. AuthInterceptor adds token.
    suspend fun fetchMerchantProfile(): Response<MerchantProfileResponse>

    // --- QR and Transaction Endpoints ---
    @POST("api/merchant-app/beneficiaries/validate-qr")
    suspend fun validateQrToken(
        @Body qrPayload: QrPayload
    ): Response<ValidatedBeneficiary>

    @POST("api/merchant-app/transactions")
    suspend fun processTransaction(
        @Body request: ProcessTransactionRequest
    ): Response<ProcessTransactionResponse>

    @GET("api/merchant-app/transactions")
    suspend fun getTransactionHistory(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("status") status: String?
    ): Response<TransactionHistoryApiResponse>
}