package com.example.sharespace.core.data.repository.dto.rooms

data class ApiRoom(
    val id: Int,
    val name: String,
    val pictureUrl: String?,
    val balanceDue: Float,
    val alerts: Int,
    val members: List<ApiRoomMember>
)