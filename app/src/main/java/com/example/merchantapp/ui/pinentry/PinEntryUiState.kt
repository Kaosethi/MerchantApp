// PinEntryUiState.kt
package com.example.merchantapp.ui.pinentry // Or your preferred package

// ADDED: Entire new file
data class PinEntryUiState(
    // Transaction details passed via navigation
    val amount: String = "",
    val beneficiaryId: String = "",
    val beneficiaryName: String = "",
    val category: String = "",

    // PIN entry specific state
    val pinValue: String = "", // Max 4 digits
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPinVerifiedSuccessfully: Boolean = false,
    val attemptsRemaining: Int = 3, // Example for attempt tracking, can be enhanced
    val isLocked: Boolean = false // If too many failed attempts
)