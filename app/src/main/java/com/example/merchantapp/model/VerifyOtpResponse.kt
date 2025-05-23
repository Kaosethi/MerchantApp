// File: app/src/main/java/com/example/merchantapp/model/VerifyOtpResponse.kt
package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class VerifyOtpResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("resetAuthorizationToken")
    val resetAuthorizationToken: String? // Nullable if verification fails without a token
    // val errors: Map<String, List<String>>? = null // Optional for detailed errors
)