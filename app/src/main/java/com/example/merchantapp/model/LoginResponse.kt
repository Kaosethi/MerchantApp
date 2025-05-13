package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

// This data class is for the nested 'merchant' object within LoginResponse
data class MerchantInfo(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("email")
    val email: String?
    // Add other relevant merchant fields returned on login if your API sends them
)

data class LoginResponse(
    @SerializedName("message")
    val message: String?,

    @SerializedName("merchant")
    val merchant: MerchantInfo?, // The merchant object

    @SerializedName("token")
    val token: String?
)