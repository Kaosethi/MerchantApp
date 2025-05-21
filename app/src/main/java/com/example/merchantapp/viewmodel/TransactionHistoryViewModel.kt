// File: app/src/main/java/com/example/merchantapp/viewmodel/TransactionHistoryViewModel.kt
package com.example.merchantapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.model.ApiTransactionItem
import com.example.merchantapp.model.DeclineReason
import com.example.merchantapp.model.TransactionItem
import com.example.merchantapp.model.TransactionStatus
import com.example.merchantapp.network.ApiService
import com.example.merchantapp.network.RetrofitInstance
import com.example.merchantapp.ui.summary.TransactionSummaryUiState // Ensure this path is correct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// Mapper function with improved null safety for date parsing
fun ApiTransactionItem.toUiTransactionItem(): TransactionItem? {
    val eventTs = this.eventTimestamp
    val recordTs = this.recordCreatedAt

    val parsedEventDateTime: LocalDateTime? = if (eventTs != null) {
        try {
            LocalDateTime.parse(eventTs, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (e: DateTimeParseException) {
            Log.e("TransactionMapper", "Failed to parse eventTimestamp: $eventTs for API leg ID: ${this.legId}", e)
            null // Indicate parsing failure
        }
    } else {
        Log.w("TransactionMapper", "eventTimestamp is NULL for API leg ID: ${this.legId}")
        null // Timestamp was null
    }

    // Fallback to recordCreatedAt ONLY if eventTimestamp was null OR its parsing failed
    val finalDateTime: LocalDateTime = parsedEventDateTime ?: run {
        if (recordTs != null) {
            try {
                Log.d("TransactionMapper", "Falling back to recordCreatedAt for API leg ID: ${this.legId}")
                LocalDateTime.parse(recordTs, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            } catch (e2: DateTimeParseException) {
                Log.e("TransactionMapper", "Also failed to parse recordCreatedAt: $recordTs for API leg ID: ${this.legId}. Returning null for mapping.", e2)
                return null // Both attempts failed or critical timestamp missing
            }
        } else {
            Log.e("TransactionMapper", "Both eventTimestamp and recordCreatedAt are NULL for API leg ID: ${this.legId}. Returning null for mapping.")
            return null // Both timestamps are null
        }
    }

    val parsedAmount = try {
        this.amount.toDouble()
    } catch (e: NumberFormatException) {
        Log.e("TransactionMapper", "Failed to parse amount: ${this.amount} for API leg ID: ${this.legId}", e)
        return null
    }

    val uiStatus: TransactionStatus = when (this.status.uppercase()) { // Use uppercase for robustness
        "COMPLETED" -> TransactionStatus.APPROVED
        "PENDING" -> TransactionStatus.PENDING
        "DECLINED" -> TransactionStatus.DECLINED
        "FAILED" -> TransactionStatus.FAILED
        else -> {
            Log.w("TransactionMapper", "Unknown transaction status from API: '${this.status}' for API leg ID: ${this.legId}")
            TransactionStatus.FAILED // Default or map to an 'UNKNOWN' status if you have one
        }
    }

    val uiDeclineReason: DeclineReason? = this.declineReason?.takeIf { it.isNotBlank() }?.let { reasonString ->
        val lowerReason = reasonString.lowercase().trim()
        when {
            lowerReason.contains("insufficient funds") || lowerReason.contains("insufficient balance") -> DeclineReason.InsufficientBalance
            lowerReason.contains("account suspended") -> DeclineReason.AccountSuspended
            lowerReason.contains("not active") || lowerReason.contains("inactive") -> DeclineReason.AccountNotActive
            lowerReason.contains("incorrect pin") -> DeclineReason.IncorrectPin
            lowerReason.contains("account not found") || lowerReason.contains("beneficiary account not found") -> DeclineReason.AccountNotFound
            else -> DeclineReason.Other(reasonString)
        }
    }

    val shortDisplayId = this.legId.substringAfterLast('-', this.legId).takeLast(8).ifEmpty { this.legId.takeLast(8) }
    val partyAccountId = this.relatedAccountDisplayId ?: this.relatedAccountId ?: "N/A"
    val partyAccountName = this.relatedAccountChildName ?: "Unknown Customer"

    return TransactionItem(
        id = shortDisplayId,
        fullTransactionId = this.legId,
        dateTime = finalDateTime,
        accountId = partyAccountId,
        accountOwnerName = partyAccountName,
        amount = parsedAmount,
        status = uiStatus,
        category = this.originalDescription ?: "Uncategorized",
        reasonForDecline = uiDeclineReason
    )
}


open class TransactionHistoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Ensure TransactionSummaryUiState has isLoading = false by default
    protected val _transactionUiState = MutableStateFlow(TransactionSummaryUiState())
    val uiState: StateFlow<TransactionSummaryUiState> = _transactionUiState.asStateFlow()

    private val apiService: ApiService = RetrofitInstance.getApiService(application.applicationContext)

    private var currentPage = 1
    private var totalPages = 1
    private var currentStatusFilter: String = "Completed" // Default status for API calls

    init {
        Log.d("TransactionHistoryVM", "ViewModel initialized. Initial isLoading state: ${_transactionUiState.value.isLoading}")
        currentStatusFilter = _transactionUiState.value.selectedStatusFilter // This usually defaults to "All" or "Completed" from UiState
        val initialApiStatusFilter = if (currentStatusFilter.equals("All", ignoreCase = true)) null else currentStatusFilter
        fetchTransactionPage(page = 1, statusFilter = initialApiStatusFilter)
    }

    fun loadMoreTransactions() {
        Log.d("TransactionHistoryVM", "loadMoreTransactions called. Current page: $currentPage, Total pages: $totalPages, isLoading: ${_transactionUiState.value.isLoading}")
        if (currentPage < totalPages && !_transactionUiState.value.isLoading) {
            Log.d("TransactionHistoryVM", "Proceeding to load more. Next page: ${currentPage + 1}")
            val statusToSend = if (currentStatusFilter.equals("All", ignoreCase = true)) null else currentStatusFilter
            fetchTransactionPage(page = currentPage + 1, statusFilter = statusToSend)
        } else {
            val reason = when {
                _transactionUiState.value.isLoading -> "already loading"
                currentPage >= totalPages -> "reached last page (current: $currentPage, total: $totalPages)"
                else -> "condition not met (currentPage: $currentPage < totalPages: $totalPages is ${currentPage < totalPages}, !isLoading is ${!_transactionUiState.value.isLoading})"
            }
            Log.d("TransactionHistoryVM", "Cannot load more because $reason.")
        }
    }

    private fun fetchTransactionPage(page: Int, statusFilter: String?, isRefresh: Boolean = false) {
        Log.d("TransactionHistoryVM", "Entering fetchTransactionPage. Current isLoading: ${_transactionUiState.value.isLoading}, isRefresh: $isRefresh, Page: $page, API Status Filter: '$statusFilter'")
        if (_transactionUiState.value.isLoading && !isRefresh) {
            Log.d("TransactionHistoryVM", "Fetch ignored: Already loading and not a refresh. Page: $page")
            return
        }

        viewModelScope.launch {
            val initialErrorMessage = if (isRefresh) null else _transactionUiState.value.errorMessage
            _transactionUiState.update { it.copy(isLoading = true, errorMessage = initialErrorMessage) }
            Log.d("TransactionHistoryVM", "Set isLoading=true. Fetching page: $page, API Status Filter: '$statusFilter', IsRefresh: $isRefresh")

            try {
                // Assuming ApiService interface has @Header("Authorization") for token or Interceptor handles it
                val response = apiService.getTransactionHistory(
                    page = page,
                    limit = 20,
                    status = statusFilter
                )

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    Log.d("TransactionHistoryVM", "API Response for page $page successful. Mapping ${apiResponse.data.size} items.")
                    val newUiTransactions = apiResponse.data.mapNotNull { it.toUiTransactionItem() }
                    val paginationDetails = apiResponse.pagination

                    totalPages = paginationDetails.totalPages
                    currentPage = paginationDetails.page

                    _transactionUiState.update { currentState ->
                        val combinedTransactions = if (isRefresh || page == 1) {
                            newUiTransactions
                        } else {
                            (currentState.allTransactions + newUiTransactions).distinctBy { it.fullTransactionId }
                        }
                        currentState.copy(
                            isLoading = false,
                            allTransactions = combinedTransactions,
                            filteredTransactions = applyLocalFilters(combinedTransactions, currentState.selectedDate, currentState.selectedStatusFilter),
                            errorMessage = null
                        )
                    }
                    Log.d("TransactionHistoryVM", "Page $page fetched. ${newUiTransactions.size} new UI items. Total allTransactions: ${_transactionUiState.value.allTransactions.size}. API Page: $currentPage, API Total Pages: $totalPages. Set isLoading=false.")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    val errorMsg = "Error fetching transactions: ${response.message()} (Code: ${response.code()})"
                    Log.e("TransactionHistoryVM", "API Error for page $page: ${response.code()} - $errorBody. Message: $errorMsg")
                    _transactionUiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                    Log.d("TransactionHistoryVM", "API Error. Set isLoading=false.")
                }
            } catch (e: Exception) {
                val errorMsg = "Network error or mapping error: ${e.message ?: "Unknown exception"}"
                Log.e("TransactionHistoryVM", "Exception for page $page: ${e.message}", e) // Log full exception
                _transactionUiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                Log.d("TransactionHistoryVM", "Exception caught. Set isLoading=false.")
            }
        }
    }

    private fun applyLocalFilters(
        sourceList: List<TransactionItem>,
        selectedDate: LocalDate?,
        statusFilterKeyFromUi: String
    ): List<TransactionItem> {
        Log.d("TransactionHistoryVM", "Applying local filters. Source size: ${sourceList.size}, Date: $selectedDate, UI Status Key: $statusFilterKeyFromUi")
        val locallyFiltered = sourceList.filter { transaction ->
            val dateMatch = selectedDate?.let { transaction.dateTime.toLocalDate() == it } ?: true

            // This client-side status filter is primarily useful if the API was called for "All" statuses.
            // If API was called for a specific status, all items in sourceList should already match that.
            val statusMatch = if (currentStatusFilter.equals("All", ignoreCase = true) || currentStatusFilter == null) {
                when (statusFilterKeyFromUi.lowercase()) { // use lowercase for robustness
                    "approved" -> transaction.status == TransactionStatus.APPROVED
                    "declined" -> transaction.status == TransactionStatus.DECLINED
                    "pending" -> transaction.status == TransactionStatus.PENDING
                    "failed" -> transaction.status == TransactionStatus.FAILED
                    "all" -> true
                    else -> true
                }
            } else {
                true
            }
            dateMatch && statusMatch
        }
        Log.d("TransactionHistoryVM", "Local filtering resulted in ${locallyFiltered.size} items.")
        return locallyFiltered
    }

    fun onDateSelected(date: LocalDate?) {
        Log.d("TransactionHistoryVM", "Date selected in UI: $date.")
        _transactionUiState.update { it.copy(selectedDate = date, showDatePickerDialog = false) }
        val currentAllTransactions = _transactionUiState.value.allTransactions
        val currentStatusFilterKeyFromUi = _transactionUiState.value.selectedStatusFilter
        _transactionUiState.update {
            it.copy(filteredTransactions = applyLocalFilters(currentAllTransactions, date, currentStatusFilterKeyFromUi))
        }
    }

    fun onStatusFilterChanged(newStatusKeyFromUi: String) {
        _transactionUiState.update { it.copy(selectedStatusFilter = newStatusKeyFromUi) }
        Log.d("TransactionHistoryVM", "UI Status filter dropdown changed to: $newStatusKeyFromUi.")
        // Note: This only updates the UI state. applyFiltersAndFetch() triggers the new API call.
    }

    fun applyFiltersAndFetch() {
        val newSelectedStatusFromUi = _transactionUiState.value.selectedStatusFilter
        currentStatusFilter = newSelectedStatusFromUi
        Log.d("TransactionHistoryVM", "Apply Filters button clicked. New API status filter set to: $currentStatusFilter. Refreshing data from page 1.")

        val statusToSendToApi = if (currentStatusFilter.equals("All", ignoreCase = true)) null else currentStatusFilter
        currentPage = 1 // Reset pagination for new filter
        totalPages = 1
        fetchTransactionPage(page = 1, statusFilter = statusToSendToApi, isRefresh = true)
    }

    fun showDatePicker(show: Boolean) {
        _transactionUiState.update { it.copy(showDatePickerDialog = show) }
    }

    fun showTransactionDetails(transaction: TransactionItem?) {
        _transactionUiState.update {
            it.copy(
                showTransactionDetailDialog = transaction != null,
                transactionForDetail = transaction
            )
        }
    }
}