package com.example.merchantapp.model // Ensure this package name is correct

import com.google.gson.annotations.SerializedName

// This data class represents the successful response from the
// POST /api/merchant-app/beneficiaries/validate-qr endpoint
data class ValidatedBeneficiary(
    @SerializedName("id") // This matches the JSON key "id" from the backend
    val id: String,       // This will hold the beneficiary's displayId (e.g., "STC-2025-685V")

    @SerializedName("name") // This matches the JSON key "name" from the backend
    val name: String        // This will hold the beneficiary's childName
)