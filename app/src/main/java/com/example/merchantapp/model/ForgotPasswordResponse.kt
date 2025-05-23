// File: app/src/main/java/com/example/merchantapp/model/ForgotPasswordResponse.kt
package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class ForgotPasswordResponse(
    @SerializedName("message")
    val message: String
    // Optionally, if your backend sends more details in case of Zod errors for example
    // val errors: Map<String, List<String>>? = null
)