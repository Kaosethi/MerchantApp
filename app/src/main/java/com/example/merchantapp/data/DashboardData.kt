
package com.example.merchantapp.data

import java.util.UUID

data class TransactionSummary(
    val count: Int,
    val totalAmount: Double
)

data class DashboardSummaryResponse(
    val merchantName: String,
    val today: TransactionSummary,
    val thisWeek: TransactionSummary,
    val thisMonth: TransactionSummary
)


data class TransactionDisplayItem(
    val id: String,
    val amount: String,
    val description: String,
    val date: String
)

// For Mock Settlements
data class SettlementItem(
    val id: String = UUID.randomUUID().toString(),
    val settlementDate: String,
    val periodDescription: String,
    val amount: String
)