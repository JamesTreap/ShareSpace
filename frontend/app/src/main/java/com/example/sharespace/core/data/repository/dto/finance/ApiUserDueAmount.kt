package com.example.sharespace.core.data.repository.dto.finance

import com.google.gson.annotations.SerializedName


data class ApiUserDueAmount(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("amount_due")
    val amountDue: Double
)