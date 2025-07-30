package com.example.sharespace.core.data.repository.dto.rooms

data class ApiUpdateRoomRequest(
    val name: String? = null,

    val description: String? = null,

    val address: String? = null
)