package com.example.sharespace.core.data.repository.dto.auth

data class ApiLoginResponse(
    val token: String? = null,
    val error: String? = null,
    val userId: String? = null,
    val message: String? = null
)
