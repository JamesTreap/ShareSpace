package com.example.sharespace.core.data.repository.dto.finance

import com.google.gson.annotations.SerializedName

data class ApiBill(
    override val id: Int,
    override val amount: Double,
    override val type: String,
    @SerializedName("created_at")
    override val createdAt: String,
    @SerializedName("payer_user_id")
    override val payerUserId: Int,

    val title: String,
    val category: String,
    @SerializedName("scheduled_date")
    val scheduledDate: String,

    @SerializedName("meta_data")
    val metadata: ApiBillMetadata?,

    val deadline: String?
) : ApiTransaction