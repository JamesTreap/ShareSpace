package com.example.sharespace.core.data.repository.dto.rooms

data class ApiRoom(
    val id: Int,
    val name: String,
    val pictureUrl: String?,
    val members: List<ApiRoomMember>,
    val address: String?,
    val description: String?,
    val balanceDue: Float? = null,
    val alerts: Int? = null
)