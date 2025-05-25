// File: app/src/main/java/com/example/merchantapp/ui/setnewpassword/SetNewPasswordUiState.kt
package com.example.merchantapp.ui.setnewpassword

data class SetNewPasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val passwordsMatch: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val apiMessage: String? = null,
    val isPasswordSetSuccessfully: Boolean = false,
    val passwordRequirementsMessage: String = "Password must be at least 8 characters.",
    val canSubmit: Boolean = false,
    val emailForContext: String? = null // Renamed from emailOrToken, specifically for email display
)