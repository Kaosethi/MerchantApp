// File: app/src/main/java/com/example/merchantapp/model/VerifyOtpRequest.kt
package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp")
    val otp: String
)