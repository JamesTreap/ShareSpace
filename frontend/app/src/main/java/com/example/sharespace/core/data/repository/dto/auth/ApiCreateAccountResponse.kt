package com.example.sharespace.core.data.repository.dto.auth

data class ApiCreateAccountResponse(
    val message: String? = null,
    val token: String? = null,
    val error: String? = null
)