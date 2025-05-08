// File: app/src/main/java/com/example/merchantapp/ui/otp/OtpEntryUiState.kt
package com.example.merchantapp.ui.otp // <<< CORRECT PACKAGE

/**
 * Represents the UI state for the OTP Entry screen.
 *
 * @property otpValue The 6-digit OTP entered by the user.
 * @property email The email address to which the OTP was supposedly sent (for display).
 * @property isLoading Indicates if OTP verification is in progress.
 * @property errorMessage Holds an error message string if an error occurred (e.g., invalid OTP).
 * @property isOtpVerified Indicates if the OTP has been successfully verified.
 * @property resendTimerSeconds The countdown timer for enabling the "Resend OTP" button (0 means enabled).
 * @property isResendEnabled True if the "Resend OTP" button should be enabled.
 */
data class OtpEntryUiState(
    val otpValue: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOtpVerified: Boolean = false,
    val resendTimerSeconds: Int = 60, // Example: 60 second countdown
    val isResendEnabled: Boolean = false
)