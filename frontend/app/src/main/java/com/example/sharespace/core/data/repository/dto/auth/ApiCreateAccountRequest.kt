package com.example.sharespace.core.data.repository.dto.auth

data class ApiCreateAccountRequest(
    val username: String,
    val password: String,
    val email: String
)

