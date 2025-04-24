package com.example.merchantapp.data

// IMPORTANT: Adjust fields below based on your ACTUAL API response!
data class LoginResponse(
    val token: String?,
    val merchantId: String?,
    val error: String?
    // Add/remove/rename fields to match the JSON keys from your backend
)