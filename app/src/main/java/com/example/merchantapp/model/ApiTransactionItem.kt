package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class ApiTransactionItem(
    @SerializedName("legId") // CORRECT: Matches JSON key "legId"
    val legId: String,

    @SerializedName("paymentId")
    val paymentId: String,

    @SerializedName("eventTimestamp") // CORRECT: Matches JSON key "eventTimestamp"
    val eventTimestamp: String?, // Make it nullable to be safe, align with mapper logic

    @SerializedName("recordCreatedAt") // CORRECT: Matches JSON key "recordCreatedAt"
    val recordCreatedAt: String?, // Make it nullable to be safe, align with mapper logic

    @SerializedName("amount")
    val amount: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("originalDescription") // CORRECT: Matches JSON key "originalDescription"
    val originalDescription: String?,

    @SerializedName("declineReason")
    val declineReason: String?,

    @SerializedName("relatedAccountId")
    val relatedAccountId: String?,

    @SerializedName("relatedAccountDisplayId")
    val relatedAccountDisplayId: String?,

    @SerializedName("relatedAccountChildName")
    val relatedAccountChildName: String?,

    @SerializedName("relatedAccountType")
    val relatedAccountType: String?,

    @SerializedName("displayDescription")
    val displayDescription: String?
)