package com.example.merchantapp.model

import com.google.gson.annotations.SerializedName

data class ValidatedBeneficiary(
    @SerializedName("accountId")
    val accountUuid: String,

    @SerializedName("accountDisplayId")
    val displayId: String,

    @SerializedName("name")
    val name: String
)
