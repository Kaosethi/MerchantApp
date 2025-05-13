package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") // Or "merchantId" or "username" depending on your API
    val email: String,

    @SerializedName("password") // Or "pin"
    val password: String
)