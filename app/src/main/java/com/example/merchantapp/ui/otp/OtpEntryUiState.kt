// File: app/src/main/java/com/example/merchantapp/ui/otp/OtpEntryUiState.kt (Example)
package com.example.merchantapp.ui.otp

const val OTP_LENGTH_CONST = 6 // Use a const if OTP_LENGTH is used in UI too

data class OtpEntryUiState(
    val email: String, // Passed from previous screen
    val otpValue: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val apiMessage: String? = null,       // For success/info messages from API
    val isOtpVerified: Boolean = false,   // True if OTP verification API call was successful
    val resetAuthToken: String? = null,   // To store the token from backend
    val isResendEnabled: Boolean = false,
    val resendTimerSeconds: Int = 0
)