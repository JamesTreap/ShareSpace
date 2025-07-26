package com.example.sharespace.core.domain.model

import com.example.sharespace.core.data.repository.dto.rooms.ApiRoomMember

data class RoomMember(
    val userId: Int
) {
    constructor(apiRoomMember: ApiRoomMember) : this(
        userId = apiRoomMember.userId
    )
}