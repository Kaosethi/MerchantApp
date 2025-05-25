// File: app/src/main/java/com/example/merchantapp/network/ApiService.kt
package com.example.merchantapp.network

import com.example.merchantapp.data.ProcessTransactionRequest
import com.example.merchantapp.data.ProcessTransactionResponse
import com.example.merchantapp.data.QrPayload
// Data classes for Forgot Password Flow
import com.example.merchantapp.model.ForgotPasswordRequest // <-- NEW IMPORT
import com.example.merchantapp.model.ForgotPasswordResponse // <-- NEW IMPORT
import com.example.merchantapp.model.VerifyOtpRequest // <-- NEW IMPORT
import com.example.merchantapp.model.VerifyOtpResponse // <-- NEW IMPORT
import com.example.merchantapp.model.ResetPasswordRequest
import com.example.merchantapp.model.ResetPasswordResponse
// Existing imports
import com.example.merchantapp.model.DashboardSummaryResponse
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

    // --- NEW Forgot Password Endpoints ---
    @POST("api/merchant-app/auth/forgot-password") // Endpoint for requesting OTP
    suspend fun requestPasswordReset(
        @Body request: ForgotPasswordRequest
    ): Response<ForgotPasswordResponse> // Backend returns a generic message

    // TODO: Add verifyOtp and resetPassword endpoints here later

    // --- Merchant Profile Endpoint ---
    @GET("api/merchant-app/profile")
    suspend fun getMerchantProfile(): Response<MerchantProfileResponse>

    // --- QR and Transaction Endpoints ---
    // ... (existing QR and transaction endpoints remain the same) ...
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
        @Query("status") status: String? = null
    ): Response<TransactionHistoryApiResponse>

    // --- NEW Dashboard Endpoint ---
    @GET("api/merchant-app/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummaryResponse>

    @POST("api/merchant-app/auth/forgot-password/verify-otp") // Endpoint for verifying OTP
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse> // Backend returns a message and resetAuthorizationToken

    @POST("api/merchant-app/auth/forgot-password/reset") // Endpoint for resetting the password
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ResetPasswordResponse> // Backend returns a generic success message
}