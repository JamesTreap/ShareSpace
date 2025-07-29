package com.example.sharespace.core.data.repository.dto.finance

data class ApiBill(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val created_at: String,
    val deadline: String?,
    val payer_user_id: Int,
    val scheduled_date: String,
    val type: String,
    val metadata: ApiBillMetadata?
)