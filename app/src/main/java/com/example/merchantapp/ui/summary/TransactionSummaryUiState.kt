// TransactionSummaryUiState.kt
package com.example.merchantapp.ui.summary

import com.example.merchantapp.model.TransactionItem
import java.time.LocalDate

// ADDED: Entire new file

data class TransactionSummaryUiState(
    val isLoading: Boolean = true,
    val allTransactions: List<TransactionItem> = emptyList(),
    val filteredTransactions: List<TransactionItem> = emptyList(),

    // Filter states
    val selectedDate: LocalDate? = null,
    val selectedStatusFilter: String = "All", // "All", "Approved", "Declined"
    val availableStatusFilters: List<String> = listOf("All", "Approved", "Declined"),

    // Dialog states
    val showDatePickerDialog: Boolean = false,
    val showTransactionDetailDialog: Boolean = false,
    val transactionForDetail: TransactionItem? = null,

    val errorMessage: String? = null
)