package com.example.sharespace.core.domain.model

data class User(
    val id: Int, val name: String, val photoUrl: String? = null, val username: String
)