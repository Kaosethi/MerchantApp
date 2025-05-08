// File: app/src/main/java/com/example/merchantapp/ui/forgotpassword/ForgotPasswordUiState.kt
package com.example.merchantapp.ui.forgotpassword // <<< CORRECTED PACKAGE

/**
 * Represents the UI state for the Forgot Password screen.
 *
 * @property email The email address entered by the user.
 * @property isLoading Indicates if a network request or background operation is in progress.
 * @property errorMessage Holds an error message string if an error occurred, otherwise null.
 * @property isSuccess Indicates if the password reset request was successfully submitted (mocked or real).
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)