package com.example.sharespace.core.domain.model

data class RoomMember(
    val userId: Int
) {
    constructor(apiRoomMember: com.example.sharespace.core.data.repository.dto.ApiRoomMember) : this(
        userId = apiRoomMember.userId
    )
}