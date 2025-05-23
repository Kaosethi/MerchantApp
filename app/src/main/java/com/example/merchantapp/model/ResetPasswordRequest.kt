// File: app/src/main/java/com/example/merchantapp/model/ResetPasswordRequest.kt
package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("resetAuthorizationToken")
    val resetAuthorizationToken: String,
    @SerializedName("newPassword")
    val newPassword: String
)