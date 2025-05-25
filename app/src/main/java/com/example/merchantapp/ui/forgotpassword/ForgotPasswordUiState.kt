// File: app/src/main/java/com/example/merchantapp/ui/forgotpassword/ForgotPasswordUiState.kt (Example)
package com.example.merchantapp.ui.forgotpassword

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,       // For client-side validation errors or API errors
    val apiMessage: String? = null,         // For success messages from the API
    val isOtpRequestSuccess: Boolean = false // True if the OTP request to backend was successful
)