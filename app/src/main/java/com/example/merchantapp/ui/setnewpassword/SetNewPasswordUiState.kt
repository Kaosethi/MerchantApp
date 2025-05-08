// SetNewPasswordUiState.kt
// Предполагаемый пакет: com.example.merchantapp.ui.setnewpassword
package com.example.merchantapp.ui.setnewpassword // Or your preferred package

// ADDED: Entire new file
data class SetNewPasswordUiState(
    val emailOrToken: String = "", // To display or use internally
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordSetSuccessfully: Boolean = false,
    val passwordRequirementsMessage: String? = "Password must be at least 8 characters long, include an uppercase letter, a lowercase letter, a digit, and a special character.", // Example requirement
    val passwordsMatch: Boolean = true // To indicate if newPassword and confirmPassword match
)