package com.example.merchantapp.data

import com.google.gson.annotations.SerializedName

data class ProcessTransactionResponse(
    @SerializedName("paymentDisplayId")
    val transactionId: String,

    @SerializedName("transactionStatus")
    val status: String,

    @SerializedName("message")
    val message: String?
)
