package com.example.sharespace.core.data.repository.dto.auth

data class ApiLoginResponse(
    val token: String? = null,
    val error: String? = null,
    val userId: String? = null, // Will map to/from "user_id" in JSON
    val message: String? = null
)
