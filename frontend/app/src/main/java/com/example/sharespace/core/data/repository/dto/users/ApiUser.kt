package com.example.sharespace.core.data.repository.dto.users

data class ApiUser(
    val id: Int,
    val name: String,
    val username: String,
    val profilePictureUrl: String?
)