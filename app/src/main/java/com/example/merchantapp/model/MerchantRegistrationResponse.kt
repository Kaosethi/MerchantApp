package com.example.merchantapp.model // Ensure this package declaration is correct

import com.google.gson.annotations.SerializedName

// Data class for the nested 'merchant' object within the MerchantRegistrationResponse
// This represents the details of the merchant as returned by the backend after registration
data class RegisteredMerchantInfo(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("location")
    val location: String?,

    @SerializedName("category")
    val category: String?,

    @SerializedName("contactEmail")
    val contactEmail: String?,

    @SerializedName("status")
    val status: String?, // e.g., "Pending" from the backend placeholder

    @SerializedName("submittedAt")
    val submittedAt: String? // ISO date string from the backend placeholder
)

// This is the main response object for the merchant registration API call
data class MerchantRegistrationResponse(
    @SerializedName("message")
    val message: String?,

    @SerializedName("merchant")
    val merchant: RegisteredMerchantInfo?, // Contains the details of the (mock) registered merchant

    // Optional: if your API might return an error field even on a 2xx response,
    // or if you want a common field for error messages parsed from error bodies.
    @SerializedName("error")
    val error: String? = null
)