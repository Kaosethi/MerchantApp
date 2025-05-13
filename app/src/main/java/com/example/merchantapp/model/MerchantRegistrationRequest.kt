package com.example.merchantapp.model // Make sure this package matches the folder

import com.google.gson.annotations.SerializedName

data class MerchantRegistrationRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("location")
    val location: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("contactEmail")
    val contactEmail: String? = null // Optional, might be same as login email
    // Add other fields if your registration form collects more data
    // that needs to be sent to the backend. E.g., phone.
)