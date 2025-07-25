package com.example.sharespace.core.data.repository.dto.rooms

data class ApiRoomInvitation(
    val inviteeUserId: Int,
    val inviterUserId: Int,
    val roomId: Int,
    val status: String
)

