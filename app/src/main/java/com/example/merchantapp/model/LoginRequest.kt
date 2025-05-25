// File path: app/src/main/java/com/example/merchantapp/model/LoginRequest.kt
package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)