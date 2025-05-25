// File: app/src/main/java/com/example/merchantapp/model/ResetPasswordResponse.kt
package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class ResetPasswordResponse(
    @SerializedName("message")
    val message: String
    // val errors: Map<String, List<String>>? = null // Optional for detailed errors
)