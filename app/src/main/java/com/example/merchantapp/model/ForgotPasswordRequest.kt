// File: app/src/main/java/com/example/merchantapp/model/ForgotPasswordRequest.kt
package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)