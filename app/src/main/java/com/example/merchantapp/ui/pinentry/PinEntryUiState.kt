package com.example.merchantapp.ui.pinentry // Adjust package if it's elsewhere

import com.example.merchantapp.viewmodel.PinEntryViewModel // For MAX_ATTEMPTS if defined there

// If MAX_ATTEMPTS is not in PinEntryViewModel companion object, define it here or pass as default
// For example, if you want to define it here:
// private const val DEFAULT_MAX_ATTEMPTS = 7

data class PinEntryUiState(
    val amount: String = "0.00",
    val beneficiaryId: String = "", // This is the Payer's displayId
    val beneficiaryName: String = "",
    val category: String = "",
    val pinValue: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPinVerifiedSuccessfully: Boolean = false, // Indicates transaction success
    val transactionIdForSuccess: String? = null, // <<< NEW FIELD ADDED HERE
    // Make sure MAX_ATTEMPTS is accessible. If it's in PinEntryViewModel.Companion:
    val attemptsRemaining: Int = PinEntryViewModel.MAX_ATTEMPTS,
    // Or if defined locally:
    // val attemptsRemaining: Int = DEFAULT_MAX_ATTEMPTS,
    val isLocked: Boolean = false
)