// MODIFIED: app/src/main/java/com/example/merchantapp/model/MerchantRegistrationRequest.kt
package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class MerchantRegistrationRequest(
    @SerializedName("storeName")
    val storeName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("location")
    val location: String, // Maps to storeAddress in backend

    // REMOVED: category property
    // val category: String,

    @SerializedName("contactPerson")
    val contactPerson: String,

    @SerializedName("contactPhoneNumber")
    val contactPhoneNumber: String
)