package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

// This data class maps directly to the "pagination" object
// from the GET /api/merchant-app/transactions backend endpoint.
data class PaginationDetails(
    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("totalItems")
    val totalItems: Int,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("hasNextPage")
    val hasNextPage: Boolean,

    @SerializedName("hasPreviousPage")
    val hasPreviousPage: Boolean,

    @SerializedName("statusFilter")
    val statusFilter: String? // The filter that was applied for this response
)