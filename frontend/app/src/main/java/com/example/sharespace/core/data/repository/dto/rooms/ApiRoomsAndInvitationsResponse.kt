package com.example.sharespace.core.data.repository.dto.rooms

data class ApiRoomsAndInvitationsResponse(
    val joinedRooms: List<ApiRoom>,
    val roomInvitations: List<ApiRoom>
)