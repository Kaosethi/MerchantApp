package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

// This data class represents the entire JSON response structure
// from the GET /api/merchant-app/transactions backend endpoint.
data class TransactionHistoryApiResponse(
    @SerializedName("data")
    val data: List<ApiTransactionItem>, // List of transaction items from the API

    @SerializedName("pagination")
    val pagination: PaginationDetails // Pagination metadata
)