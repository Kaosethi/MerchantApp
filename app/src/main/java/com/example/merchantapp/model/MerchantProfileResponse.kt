package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class MerchantProfileResponse(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("balance")
    val balance: Double?
    // Add any other fields your /api/merchant-app/profile endpoint will return
)