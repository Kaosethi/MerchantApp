package com.example.merchantapp.ui.summary

import com.example.merchantapp.model.TransactionItem
import java.time.LocalDate

data class TransactionSummaryUiState(
    val isLoading: Boolean = false,
    val allTransactions: List<TransactionItem> = emptyList(),
    val filteredTransactions: List<TransactionItem> = emptyList(),


    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val selectedStatusFilter: String = "All",
    val availableStatusFilters: List<String> = listOf("All", "Approved", "Pending", "Declined", "Failed"), // Updated to include more common statuses


    val showDatePickerDialog: Boolean = false,
    val showTransactionDetailDialog: Boolean = false,
    val transactionForDetail: TransactionItem? = null,

    val errorMessage: String? = null
)