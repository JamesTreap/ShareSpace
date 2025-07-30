package com.example.sharespace.core.data.repository.dto.finance

import com.google.gson.annotations.SerializedName


data class ApiUserDueAmount(
    @SerializedName("user_id") // <--- CRITICAL: Add this
    val userId: Int,

    @SerializedName("amount_due") // <--- CRITICAL: Add this
    val amountDue: Double
)