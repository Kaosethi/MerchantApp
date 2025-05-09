// TransactionSuccessViewModel.kt
package com.example.merchantapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.merchantapp.ui.transactionsuccess.TransactionSuccessUiState // Ensure this import path is correct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ADDED: Entire new file
class TransactionSuccessViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionSuccessUiState())
    val uiState: StateFlow<TransactionSuccessUiState> = _uiState.asStateFlow()

    init {
        val amount = savedStateHandle.get<String>("amount") ?: "N/A"
        val beneficiaryId = savedStateHandle.get<String>("beneficiaryId") ?: "N/A"
        val beneficiaryName = savedStateHandle.get<String>("beneficiaryName") ?: "N/A"
        val category = savedStateHandle.get<String>("category") ?: "N/A"
        // Note: If category was URL-encoded, decode it here.
        val transactionId = savedStateHandle.get<String>("transactionId") ?: "N/A"

        _uiState.update {
            it.copy(
                amount = amount,
                beneficiaryId = beneficiaryId,
                beneficiaryName = beneficiaryName,
                category = category,
                transactionId = transactionId
            )
        }
    }
}