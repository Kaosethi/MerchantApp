// File path: app/src/main/java/com/example/merchantapp/model/LoginResponse.kt
package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message")
    val message: String?,
    @SerializedName("merchant")
    val merchant: Merchant?,
    @SerializedName("token")
    val token: String?,
    @SerializedName("error")
    val error: String?
)

data class Merchant(
    @SerializedName("id")
    val id: String,
    @SerializedName("businessName")
    val businessName: String?,
    @SerializedName("contactPerson")
    val contactPerson: String?,
    @SerializedName("contactEmail")
    val contactEmail: String?,
    @SerializedName("contactPhone")
    val contactPhone: String?,
    @SerializedName("storeAddress")
    val storeAddress: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("submittedAt")
    val submittedAt: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("website")
    val website: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("logoUrl")
    val logoUrl: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("pinVerified")
    val pinVerified: Boolean?
)