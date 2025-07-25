package com.example.sharespace.core.data.repository.dto.rooms

data class ApiRoomInvitesResponse(
    val invitedRooms: List<ApiRoom> // Assuming invited rooms have the same structure as ApiRoom
)