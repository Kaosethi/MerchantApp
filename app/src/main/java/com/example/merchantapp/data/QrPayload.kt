// QrPayload.kt
package com.example.merchantapp.data // Or com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class QrPayload(
    @SerializedName("type")
    val type: String?,

    @SerializedName("account")
    val account: String, // Should be non-nullable if it's always present and required

    @SerializedName("ver")
    val version: String?, // Changed to String? to match JSON "1.0" and Zod schema

    @SerializedName("sig")
    val signature: String?
)