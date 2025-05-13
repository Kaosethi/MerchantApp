package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

// This class directly maps to the JSON structure from the
// GET /api/merchant-app/transactions endpoint
data class TransactionApiResponse(
    @SerializedName("id") // This will map to 'fullTransactionId' in TransactionItem
    val id: String?,

    @SerializedName("shortId") // This will map to 'id' in TransactionItem
    val shortId: String?,

    @SerializedName("dateTime") // ISO String from API (e.g., "2024-05-15T10:30:00Z")
    val dateTime: String?,

    @SerializedName("accountId")
    val accountId: String?,

    @SerializedName("accountOwnerName")
    val accountOwnerName: String?,

    @SerializedName("amount")
    val amount: Double?,

    @SerializedName("status") // String from API: e.g., "Completed", "Pending", "Declined", "Failed"
    val status: String?,

    @SerializedName("type") // E.g., "Debit", "Credit" - from API
    val type: String?,

    @SerializedName("category")
    val category: String?,

    @SerializedName("declineReason") // String from API or null
    val declineReason: String?
)