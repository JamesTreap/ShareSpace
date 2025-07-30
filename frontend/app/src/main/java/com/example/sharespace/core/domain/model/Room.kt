package com.example.sharespace.core.domain.model

import com.example.sharespace.core.data.repository.dto.rooms.ApiRoom

data class Room(
    val id: Int,
    val name: String,
    val pictureUrl: String?,
    val balanceDue: Float,
    val alerts: Int,
    val members: List<RoomMember>,
    val address: String,
    val description: String
) {
    // Secondary constructor that takes an ApiRoom object
    constructor(apiRoom: ApiRoom) : this(
        id = apiRoom.id,
        name = apiRoom.name,
        pictureUrl = apiRoom.pictureUrl,
        balanceDue = apiRoom.balanceDue ?: 0.0f,
        alerts = apiRoom.alerts ?: 0,
        members = apiRoom.members.map { apiMember ->
            RoomMember(apiMember)
        },
        address = apiRoom.address ?: "",
        description = apiRoom.description ?: ""
    )
}