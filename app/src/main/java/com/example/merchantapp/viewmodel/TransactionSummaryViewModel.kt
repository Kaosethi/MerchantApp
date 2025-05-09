// TransactionSummaryViewModel.kt
package com.example.merchantapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.model.DeclineReason
import com.example.merchantapp.model.TransactionItem
import com.example.merchantapp.model.TransactionStatus
import com.example.merchantapp.ui.summary.TransactionSummaryUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
// import java.time.LocalTime // REMOVED: Unused import
import kotlin.random.Random

// MODIFIED: Make the class open so PreviewTransactionSummaryViewModel can inherit from it
open class TransactionSummaryViewModel : ViewModel() {

    // MODIFIED: Expose _uiState for the Preview ViewModel to set mock data
    // This is a common pattern for previewing ViewModels that manage their own state loading.
    // Alternatively, the Preview ViewModel could call public methods if they existed to set state.
    protected val _uiState = MutableStateFlow(TransactionSummaryUiState())
    val uiState: StateFlow<TransactionSummaryUiState> = _uiState.asStateFlow()

    init {
        // Only fetch if not already populated by a subclass (like Preview ViewModel)
        if (_uiState.value == TransactionSummaryUiState()) { // Check if it's the default initial state
            fetchTransactions()
        }
    }

    private fun fetchTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1500) // Simulate network delay

            val mockTransactions = generateMockTransactions()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    allTransactions = mockTransactions,
                    filteredTransactions = mockTransactions
                )
            }
        }
    }

    fun onDateSelected(date: LocalDate?) {
        _uiState.update { it.copy(selectedDate = date, showDatePickerDialog = false) }
        applyFilters()
    }

    fun onStatusFilterChanged(newStatus: String) {
        _uiState.update { it.copy(selectedStatusFilter = newStatus) }
    }

    fun applyFilters() {
        _uiState.update { it.copy(isLoading = true) }

        val currentState = _uiState.value
        val filteredList = currentState.allTransactions.filter { transaction ->
            val dateMatch = currentState.selectedDate?.let {
                transaction.dateTime.toLocalDate() == it
            } ?: true

            val statusMatch = when (currentState.selectedStatusFilter) {
                "Approved" -> transaction.status == TransactionStatus.APPROVED
                "Declined" -> transaction.status == TransactionStatus.DECLINED
                "All" -> true
                else -> true
            }
            dateMatch && statusMatch
        }
        _uiState.update {
            it.copy(
                filteredTransactions = filteredList,
                isLoading = false
            )
        }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedDate = null,
                selectedStatusFilter = "All",
                filteredTransactions = it.allTransactions
            )
        }
        // applyFilters() // Optionally re-apply to show all after clearing
    }

    fun showDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePickerDialog = show) }
    }

    fun showTransactionDetails(transaction: TransactionItem?) {
        _uiState.update {
            it.copy(
                showTransactionDetailDialog = transaction != null,
                transactionForDetail = transaction
            )
        }
    }

    private fun generateMockTransactions(): List<TransactionItem> {
        val names = listOf("Debby Low", "Debby L.", "Alice D.", "Bob L.", "Charlie M.", "Eve S.", "Frank G.")
        val accountPrefixes = listOf("LOW-BAL-", "STC-2023-", "STC-2024-", "ACC-NEW-")
        val categories = listOf("Groceries", "Household", "Transport", "Utilities", "Education", "Healthcare", "Other", "Dining", "Entertainment")
        val declineReasons = listOf(
            DeclineReason.InsufficientBalance,
            DeclineReason.AccountInactive,
            DeclineReason.AccountNotFound,
            DeclineReason.IncorrectPin
        )

        return List(20) { index ->
            val randomStatus = if (Random.nextBoolean()) TransactionStatus.APPROVED else TransactionStatus.DECLINED
            val randomDateTime = LocalDateTime.now().minusDays(Random.nextLong(0, 30)).minusHours(Random.nextLong(0,23)).withMinute(Random.nextInt(0,59))
            val accountOwner = names.random()
            val accountIdShort = Random.nextInt(1, 999).toString().padStart(3, '0')
            val accountId = accountPrefixes.random() + accountIdShort

            TransactionItem(
                id = "T${1000 + index}",
                fullTransactionId = "FTXN-MERCH001-${System.currentTimeMillis() + index}",
                dateTime = randomDateTime,
                accountId = accountId,
                accountOwnerName = accountOwner,
                amount = Random.nextDouble(1.0, 100.0),
                status = randomStatus,
                category = categories.random(),
                reasonForDecline = if (randomStatus == TransactionStatus.DECLINED) declineReasons.random() else null
            )
        }.sortedByDescending { it.dateTime }
    }
}