package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName
import java.util.UUID // For mock SettlementItem

// Matches the "today", "thisWeek", "thisMonth" objects in the API response
data class TransactionSummary(
    @SerializedName("totalAmount")
    val totalAmount: Double,

    @SerializedName("count")
    val count: Int
)

// Matches the overall JSON response from GET /api/merchant-app/dashboard/summary
data class DashboardSummaryResponse(
    @SerializedName("merchantName")
    val merchantName: String,

    @SerializedName("today")
    val today: TransactionSummary,

    @SerializedName("thisWeek")
    val thisWeek: TransactionSummary,

    @SerializedName("thisMonth")
    val thisMonth: TransactionSummary
)

// For mock Recent Settlements display
data class SettlementItem(
    val id: String = UUID.randomUUID().toString(),
    val settlementDate: String, // e.g., "Feb 01, 2024"
    val periodDescription: String, // e.g., "Settlement for Jan 1-31, 2024"
    val amount: String // e.g., "₹5,780.50"  (Formatted currency string)
)

// We'll reuse ApiTransactionItem for recent transactions,
// but for display, we might want a simpler version or to map it.
// For now, let's assume ApiTransactionItem has enough info (amount, date, description).
// If not, we can create a dedicated `DashboardRecentTransactionItem` and map to it.