package com.example.merchantapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantapp.model.ApiTransactionItem
import com.example.merchantapp.model.DashboardSummaryResponse
import com.example.merchantapp.model.SettlementItem
import com.example.merchantapp.network.RetrofitInstance
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// UI State for the Dashboard
data class DashboardScreenState(
    val isLoadingSummary: Boolean = true,
    val isLoadingRecentTransactions: Boolean = true,
    val merchantName: String = "",
    val todaySummary: com.example.merchantapp.model.TransactionSummary? = null,
    val thisWeekSummary: com.example.merchantapp.model.TransactionSummary? = null,
    val thisMonthSummary: com.example.merchantapp.model.TransactionSummary? = null,
    val recentTransactions: List<ApiTransactionItem> = emptyList(),
    val recentSettlements: List<SettlementItem> = emptyList(),
    val errorMessage: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitInstance.getApiService(application.applicationContext)

    private val _uiState = MutableStateFlow(DashboardScreenState())
    val uiState: StateFlow<DashboardScreenState> = _uiState.asStateFlow()


    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("th", "TH"))

    init {
        loadDashboardData()
    }

    fun refreshData() {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Reset loading states and errors
            _uiState.update {
                it.copy(
                    isLoadingSummary = true,
                    isLoadingRecentTransactions = true,
                    errorMessage = null
                )
            }

            try {
                // Fetch data in parallel using async
                coroutineScope {
                    val summaryDeferred = async { apiService.getDashboardSummary() }
                    // Fetch last 3 transactions. Assuming default sort from API is most recent.
                    // Page 1, limit 3. Status null or "All" for all completed/failed.
                    val recentTransactionsDeferred = async {
                        apiService.getTransactionHistory(page = 1, limit = 3, status = "All") // "All" or null
                    }

                    // Await summary response
                    val summaryResponse = summaryDeferred.await()
                    if (summaryResponse.isSuccessful && summaryResponse.body() != null) {
                        val summaryData = summaryResponse.body()!!
                        _uiState.update {
                            it.copy(
                                merchantName = summaryData.merchantName,
                                todaySummary = summaryData.today,
                                thisWeekSummary = summaryData.thisWeek,
                                thisMonthSummary = summaryData.thisMonth,
                                isLoadingSummary = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                errorMessage = "Failed to load summary: ${summaryResponse.message()}",
                                isLoadingSummary = false
                            )
                        }
                    }

                    // Await recent transactions response
                    val recentTransactionsResponse = recentTransactionsDeferred.await()
                    if (recentTransactionsResponse.isSuccessful && recentTransactionsResponse.body() != null) {
                        _uiState.update {
                            it.copy(
                                recentTransactions = recentTransactionsResponse.body()!!.data,
                                isLoadingRecentTransactions = false
                            )
                        }
                    } else {
                        // Update error but don't overwrite summary error if it exists
                        _uiState.update {
                            it.copy(
                                errorMessage = it.errorMessage ?: "Failed to load recent transactions: ${recentTransactionsResponse.message()}",
                                isLoadingRecentTransactions = false
                            )
                        }
                    }
                }
                // Generate mock settlements after other data is loaded or attempted
                _uiState.update { it.copy(recentSettlements = generateMockSettlements()) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Error loading dashboard: ${e.localizedMessage}",
                        isLoadingSummary = false,
                        isLoadingRecentTransactions = false
                    )
                }
            }
        }
    }

    private fun generateMockSettlements(): List<SettlementItem> {
        val settlements = mutableListOf<SettlementItem>()
        val today = LocalDate.now()
        val monthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
        val dayMonthYearFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        val monthDayRangeFormatter = DateTimeFormatter.ofPattern("MMM dd")


        // Example: Settlement for last month
        val lastMonthPeriod = YearMonth.from(today.minusMonths(1))
        settlements.add(
            SettlementItem(
                settlementDate = today.withDayOfMonth(1).format(dayMonthYearFormatter), // Settled on 1st of current month
                periodDescription = "For ${lastMonthPeriod.atDay(1).format(monthDayRangeFormatter)} - ${lastMonthPeriod.atEndOfMonth().format(monthDayRangeFormatter)}, ${lastMonthPeriod.year}",
                amount = currencyFormatter.format(Math.random() * 5000 + 10000) // Random amount
            )
        )

        // Example: Settlement for month before last
        val twoMonthsAgoPeriod = YearMonth.from(today.minusMonths(2))
        settlements.add(
            SettlementItem(
                settlementDate = today.minusMonths(1).withDayOfMonth(1).format(dayMonthYearFormatter), // Settled on 1st of last month
                periodDescription = "For ${twoMonthsAgoPeriod.atDay(1).format(monthDayRangeFormatter)} - ${twoMonthsAgoPeriod.atEndOfMonth().format(monthDayRangeFormatter)}, ${twoMonthsAgoPeriod.year}",
                amount = currencyFormatter.format(Math.random() * 4000 + 8000)
            )
        )
        return settlements.sortedByDescending { LocalDate.parse(it.settlementDate, dayMonthYearFormatter) }
    }
}