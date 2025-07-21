package com.example.sharespace.core.domain.model

import com.example.sharespace.core.data.repository.dto.ApiRoom

data class Room(
    val id: Int,
    val name: String,
    val pictureUrl: String?,
    val balanceDue: Float,
    val alerts: Int,
    val members: List<RoomMember>
) {
    // Secondary constructor that takes an ApiRoom object
    constructor(apiRoom: ApiRoom) : this(
        id = apiRoom.id,
        name = apiRoom.name,
        pictureUrl = apiRoom.pictureUrl,
        balanceDue = apiRoom.balanceDue,
        alerts = apiRoom.alerts,
        members = apiRoom.members.map { apiMember -> // Map each ApiRoomMember to RoomMember
            RoomMember(apiMember) // Uses the secondary constructor of RoomMember
        }
    )
}