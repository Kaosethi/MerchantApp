// app/src/main/java/com/example/merchantapp/data/ProcessTransactionRequest.kt
package com.example.merchantapp.data

import com.google.gson.annotations.SerializedName

data class ProcessTransactionRequest(
    @SerializedName("beneficiaryDisplayId") // MATCHES BACKEND
    val beneficiaryDisplayId: String,

    @SerializedName("enteredPin") // MATCHES BACKEND
    val enteredPin: String,

    @SerializedName("amount")
    val amount: String, // Backend parses to number, string is fine from client

    @SerializedName("description") // Use category as description, or pass category separately
    val description: String, // Formerly 'category'

    // Optional: If you implement client-side max attempt reporting
    // @SerializedName("clientReportedPinFailureType")
    // val clientReportedPinFailureType: String? = null
)