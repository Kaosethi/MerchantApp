package com.example.merchantapp.data

import com.google.gson.annotations.SerializedName

data class ProcessTransactionResponse(
    @SerializedName("transactionId")
    val transactionId: String,

    @SerializedName("status")
    val status: String, // e.g., "Completed", "Failed", "Pending"

    @SerializedName("message")
    val message: String? // Optional message from the backend
)