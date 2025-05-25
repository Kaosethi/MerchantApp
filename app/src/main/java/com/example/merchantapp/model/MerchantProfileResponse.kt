// NEW or MODIFIED: app/src/main/java/com/example/merchantapp/model/MerchantProfileResponse.kt
package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

// This class directly represents the JSON object returned by the /profile endpoint
data class MerchantProfileResponse(
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
    val submittedAt: String?, // Expecting ISO date string

    @SerializedName("category")
    val category: String?,

    @SerializedName("website")
    val website: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("logoUrl")
    val logoUrl: String?,

    @SerializedName("createdAt")
    val createdAt: String?, // Expecting ISO date string

    @SerializedName("updatedAt")
    val updatedAt: String?, // Expecting ISO date string

    @SerializedName("pinVerified")
    val pinVerified: Boolean?,

    @SerializedName("declineReason") // Added this as it's in your schema
    val declineReason: String?,

    // Fields for an error response (if the backend sends error within a 200 OK, less common for GET)
    // Or if you parse errorBody into this same type.
    @SerializedName("error")
    val error: String? = null
)