// QrPayload.kt
package com.example.merchantapp.data // Or your model package

import com.google.gson.annotations.SerializedName

// ADDED: Entire new file
data class QrPayload(
    @SerializedName("type") // Ensure Gson uses the correct JSON key
    val type: String?,

    @SerializedName("account")
    val account: String?,

    @SerializedName("ver")
    val version: Int?, // Assuming 'ver' is an integer version

    @SerializedName("sig")
    val signature: String?
)