package com.example.sharespace.core.data.repository.dto.finance

sealed interface ApiTransaction {
    val id: Int
    val amount: Double
    val createdAt: String
    val payerUserId: Int
    val type: String
}