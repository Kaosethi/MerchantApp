// TransactionSuccessUiState.kt
package com.example.merchantapp.ui.transactionsuccess // Or your preferred package

// ADDED: Entire new file
data class TransactionSuccessUiState(
    val amount: String = "",
    val beneficiaryId: String = "",
    val beneficiaryName: String = "",
    val category: String = "", // This is the description you mentioned
    val transactionId: String = ""
)