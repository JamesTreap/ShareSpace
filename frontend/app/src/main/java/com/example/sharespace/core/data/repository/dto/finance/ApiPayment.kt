package com.example.sharespace.core.data.repository.dto.finance
data class ApiPayment(
    override val id: Int,
    override val amount: Double,
    override val type: String,
    override val createdAt: String,
    override val payerUserId: Int,
    val payeeUserId: Int
) : ApiTransaction