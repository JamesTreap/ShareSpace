package com.example.sharespace.core.data.repository.dto.users

data class ApiPatchProfileRequest(
    val name: String,
    val username: String,
    val profile_picture_url: String
)