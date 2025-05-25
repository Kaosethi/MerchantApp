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

// ApiTransactionItem.toUiTransactionItem() function remains the same
fun ApiTransactionItem.toUiTransactionItem(): TransactionItem? {
    val eventTs = this.eventTimestamp
    val recordTs = this.recordCreatedAt

    val parsedEventDateTime: LocalDateTime? = if (eventTs != null) {
        try {
            LocalDateTime.parse(eventTs, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (e: DateTimeParseException) {
            Log.e("TransactionMapper", "Failed to parse eventTimestamp: $eventTs for API leg ID: ${this.legId}", e)
            null
        }
    } else {
        Log.w("TransactionMapper", "eventTimestamp is NULL for API leg ID: ${this.legId}")
        null
    }

    val finalDateTime: LocalDateTime = parsedEventDateTime ?: run {
        if (recordTs != null) {
            try {
                Log.d("TransactionMapper", "Falling back to recordCreatedAt for API leg ID: ${this.legId}")
                LocalDateTime.parse(recordTs, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            } catch (e2: DateTimeParseException) {
                Log.e("TransactionMapper", "Also failed to parse recordCreatedAt: $recordTs for API leg ID: ${this.legId}. Returning null for mapping.", e2)
                return null
            }
        } else {
            Log.e("TransactionMapper", "Both eventTimestamp and recordCreatedAt are NULL for API leg ID: ${this.legId}. Returning null for mapping.")
            return null
        }
    }

    val parsedAmount = try {
        this.amount.toDouble()
    } catch (e: NumberFormatException) {
        Log.e("TransactionMapper", "Failed to parse amount: ${this.amount} for API leg ID: ${this.legId}", e)
        return null
    }

    val uiStatus: TransactionStatus = when (this.status.uppercase()) {
        "COMPLETED" -> TransactionStatus.APPROVED
        "PENDING" -> TransactionStatus.PENDING
        "DECLINED" -> TransactionStatus.DECLINED
        "FAILED" -> TransactionStatus.FAILED
        else -> {
            Log.w("TransactionMapper", "Unknown transaction status from API: '${this.status}' for API leg ID: ${this.legId}")
            TransactionStatus.FAILED
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

    protected val _transactionUiState = MutableStateFlow(TransactionSummaryUiState())
    val uiState: StateFlow<TransactionSummaryUiState> = _transactionUiState.asStateFlow()

    private val apiService: ApiService = RetrofitInstance.getApiService(application.applicationContext)

    private var currentPage = 1
    private var totalPages = 1

    init {
        Log.d("TransactionHistoryVM", "ViewModel initialized. Initial UI filters: Status='${_transactionUiState.value.selectedStatusFilter}', StartDate='${_transactionUiState.value.selectedStartDate}', EndDate='${_transactionUiState.value.selectedEndDate}'")
        applyFiltersAndFetch()
    }

    fun loadMoreTransactions() {
        Log.d("TransactionHistoryVM", "loadMoreTransactions called. Current page: $currentPage, Total pages: $totalPages, isLoading: ${_transactionUiState.value.isLoading}")
        if (currentPage < totalPages && !_transactionUiState.value.isLoading) {
            Log.d("TransactionHistoryVM", "Proceeding to load more. Next page: ${currentPage + 1}")
            val uiSelectedStatus = _transactionUiState.value.selectedStatusFilter
            val statusToSendToApi = mapUiStatusToApiParam(uiSelectedStatus)
            // Date range is not sent to API in this version, applied locally
            fetchTransactionPage(page = currentPage + 1, statusFilter = statusToSendToApi)
        } else { /* ... logging ... */ }
    }

    fun clearAllFiltersAndFetch() {
        _transactionUiState.value = _transactionUiState.value.copy(
            selectedStartDate = null,
            selectedEndDate = null,
            selectedStatusFilter = "All"
        )
        refreshTransactions()
    }


    private fun fetchTransactionPage(
        page: Int,
        statusFilter: String?,
        // startDateFilter: String? = null, // Add if API supports date range
        // endDateFilter: String? = null,   // Add if API supports date range
        isRefresh: Boolean = false
    ) {
        Log.d("TransactionHistoryVM", "Entering fetchTransactionPage. isRefresh: $isRefresh, Page: $page, API Status Filter: '$statusFilter'")
        if (_transactionUiState.value.isLoading && !isRefresh) {
            Log.d("TransactionHistoryVM", "Fetch ignored: Already loading and not a refresh. Page: $page")
            return
        }

        viewModelScope.launch {
            val errorMessageToPreserve = if (isRefresh) null else _transactionUiState.value.errorMessage
            _transactionUiState.update { it.copy(isLoading = true, errorMessage = errorMessageToPreserve) }
            Log.d("TransactionHistoryVM", "Set isLoading=true. Fetching page: $page, API Status Filter: '$statusFilter'")

            try {
                val response = apiService.getTransactionHistory(
                    page = page,
                    limit = 20,
                    status = statusFilter
                    // startDate = startDateFilter, // Pass to API if supported
                    // endDate = endDateFilter    // Pass to API if supported
                )

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
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
                        val locallyFilteredTransactions = applyLocalFilters(
                            combinedTransactions,
                            currentState.selectedStartDate, // Pass startDate
                            currentState.selectedEndDate,   // Pass endDate
                            currentState.selectedStatusFilter
                        )
                        currentState.copy(
                            isLoading = false,
                            allTransactions = combinedTransactions,
                            filteredTransactions = locallyFilteredTransactions,
                            errorMessage = null
                        )
                    }
                } else { /* ... error handling ... */
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    val errorMsg = "Failed to load transactions. Server error. (Code: ${response.code()})"
                    Log.e("TransactionHistoryVM", "API Error for page $page: ${response.code()} - $errorBody. Full Message: ${response.message()}")
                    _transactionUiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                }
            } catch (e: Exception) { /* ... error handling ... */
                val errorMsg = "Could not connect or an error occurred. Please try again."
                Log.e("TransactionHistoryVM", "Exception for page $page: ${e.message}", e)
                _transactionUiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
            }
        }
    }

    private fun mapUiStatusToApiParam(uiStatusFilter: String): String? {
        // ... (remains the same as your last version) ...
        return when (uiStatusFilter.lowercase()) {
            "all" -> null
            "approved" -> "COMPLETED"
            "pending" -> "PENDING"
            "declined" -> "FAILED"
            "failed" -> "FAILED"
            else -> {
                Log.w("TransactionHistoryVM", "Unknown UI status filter for API mapping: '$uiStatusFilter', sending null.")
                null
            }
        }
    }

    // --- MODIFIED applyLocalFilters to accept startDate and endDate ---
    private fun applyLocalFilters(
        sourceList: List<TransactionItem>,
        selectedStartDate: LocalDate?,
        selectedEndDate: LocalDate?,
        statusFilterKeyFromUi: String
    ): List<TransactionItem> {
        Log.d("TransactionHistoryVM", "Applying local filters. Source size: ${sourceList.size}, StartDate: $selectedStartDate, EndDate: $selectedEndDate, UI Status Key: $statusFilterKeyFromUi")
        val locallyFiltered = sourceList.filter { transaction ->
            val transactionDate = transaction.dateTime.toLocalDate()

            // Date range matching logic
            val dateMatch = when {
                selectedStartDate != null && selectedEndDate != null -> {
                    !transactionDate.isBefore(selectedStartDate) && !transactionDate.isAfter(selectedEndDate)
                }
                selectedStartDate != null -> { // Only start date selected
                    !transactionDate.isBefore(selectedStartDate)
                }
                selectedEndDate != null -> { // Only end date selected (less common, but handle)
                    !transactionDate.isAfter(selectedEndDate)
                }
                else -> true // No date range selected
            }

            val statusMatch = when (statusFilterKeyFromUi.lowercase()) {
                "all" -> true
                "approved" -> transaction.status == TransactionStatus.APPROVED
                "pending" -> transaction.status == TransactionStatus.PENDING
                "declined" -> transaction.status == TransactionStatus.FAILED || transaction.status == TransactionStatus.DECLINED
                "failed" -> transaction.status == TransactionStatus.FAILED
                else -> true
            }
            dateMatch && statusMatch
        }
        Log.d("TransactionHistoryVM", "Local filtering resulted in ${locallyFiltered.size} items.")
        return locallyFiltered
    }
    // --- END MODIFIED applyLocalFilters ---

    // --- NEW: onDateRangeSelected function ---
    fun onDateRangeSelected(startDate: LocalDate?, endDate: LocalDate?) {
        Log.d("TransactionHistoryVM", "Date range selected in UI: StartDate=$startDate, EndDate=$endDate")
        _transactionUiState.update { currentState ->
            val newFiltered = applyLocalFilters(
                currentState.allTransactions,
                startDate, // Use new startDate
                endDate,   // Use new endDate
                currentState.selectedStatusFilter
            )
            currentState.copy(
                selectedStartDate = startDate,
                selectedEndDate = endDate,
                showDatePickerDialog = false, // Close dialog
                filteredTransactions = newFiltered
            )
        }
    }
    // --- END NEW onDateRangeSelected ---

    // `onDateSelected` is no longer needed, remove or comment out
    /*
    fun onDateSelected(date: LocalDate?) { ... }
    */

    fun onStatusFilterChanged(newStatusKeyFromUi: String) {
        _transactionUiState.update { currentState ->
            val newFiltered = applyLocalFilters(
                currentState.allTransactions,
                currentState.selectedStartDate, // Use existing startDate
                currentState.selectedEndDate,   // Use existing endDate
                newStatusKeyFromUi
            )
            currentState.copy(
                selectedStatusFilter = newStatusKeyFromUi,
                filteredTransactions = newFiltered
            )
        }
        Log.d("TransactionHistoryVM", "UI Status filter selection changed to: $newStatusKeyFromUi. Local filter applied.")
    }

    fun applyFiltersAndFetch() {
        val uiStateValue = _transactionUiState.value
        Log.d("TransactionHistoryVM", "Apply Filters. UI Status: ${uiStateValue.selectedStatusFilter}, StartDate: ${uiStateValue.selectedStartDate}, EndDate: ${uiStateValue.selectedEndDate}")

        val statusToSendToApi = mapUiStatusToApiParam(uiStateValue.selectedStatusFilter)
        // val startDateToSendToApi = uiStateValue.selectedStartDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) // If API supports
        // val endDateToSendToApi = uiStateValue.selectedEndDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)     // If API supports

        Log.d("TransactionHistoryVM", "Mapped API status: '$statusToSendToApi'. Refreshing data.")
        currentPage = 1
        totalPages = 1
        fetchTransactionPage(
            page = 1,
            statusFilter = statusToSendToApi,
            // startDateFilter = startDateToSendToApi, // Pass to API if supported
            // endDateFilter = endDateToSendToApi,     // Pass to API if supported
            isRefresh = true
        )
    }

    fun showDatePicker(show: Boolean) { // This function now controls the visibility of the DateRangePickerDialog
        _transactionUiState.update { it.copy(showDatePickerDialog = show) }
    }

    fun showTransactionDetails(transaction: TransactionItem?) { /* ... */ }
    fun refreshTransactions() { applyFiltersAndFetch() }
}