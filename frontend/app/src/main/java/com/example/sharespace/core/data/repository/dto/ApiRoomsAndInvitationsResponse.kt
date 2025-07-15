package com.example.sharespace.core.data.repository.dto

data class ApiRoomsAndInvitationsResponse(
    val joinedRooms: List<ApiRoom>,
    val roomInvitations: List<ApiRoomInvitation>
)