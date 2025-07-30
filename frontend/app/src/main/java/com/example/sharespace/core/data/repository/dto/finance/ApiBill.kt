package com.example.sharespace.core.data.repository.dto.finance

import com.google.gson.annotations.SerializedName

data class ApiBill(
    override val id: Int,
    override val amount: Double,
    override val type: String,
    @SerializedName("created_at") // Good practice, though policy should cover
    override val createdAt: String,
    @SerializedName("payer_user_id") // Good practice
    override val payerUserId: Int,

    val title: String,
    val category: String,
    @SerializedName("scheduled_date") // Good practice
    val scheduledDate: String,

    @SerializedName("meta_data") // <--- CRITICAL: Add this
    val metadata: ApiBillMetadata?,

    val deadline: String?
) : ApiTransaction