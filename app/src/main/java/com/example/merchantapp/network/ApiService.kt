// File: app/src/main/java/com/example/merchantapp/network/ApiService.kt
package com.example.merchantapp.network

import com.example.merchantapp.data.ProcessTransactionRequest
import com.example.merchantapp.data.ProcessTransactionResponse
import com.example.merchantapp.data.QrPayload
import com.example.merchantapp.model.DashboardSummaryResponse // <-- NEW IMPORT
import com.example.merchantapp.model.LoginRequest
import com.example.merchantapp.model.LoginResponse
import com.example.merchantapp.model.MerchantProfileResponse
import com.example.merchantapp.model.MerchantRegistrationRequest
import com.example.merchantapp.model.MerchantRegistrationResponse
import com.example.merchantapp.model.TransactionHistoryApiResponse
import com.example.merchantapp.model.ValidatedBeneficiary
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // --- Merchant Auth Endpoints ---
    @POST("api/merchant-app/auth/register")
    suspend fun registerMerchant(
        @Body request: MerchantRegistrationRequest
    ): Response<MerchantRegistrationResponse>

    @POST("api/merchant-app/auth/login")
    suspend fun loginMerchant(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // --- Merchant Profile Endpoint ---
    @GET("api/merchant-app/profile")
    suspend fun getMerchantProfile(): Response<MerchantProfileResponse>

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
        @Query("status") status: String? = null // Made status nullable and default if desired
    ): Response<TransactionHistoryApiResponse>

    // --- NEW Dashboard Endpoint ---
    @GET("api/merchant-app/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummaryResponse>
}