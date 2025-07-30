package com.example.sharespace.core.domain.model

import com.example.sharespace.core.data.repository.dto.rooms.ApiRoomInvitation

data class RoomInvitation(
    val inviteeUserId: Int,
    val inviterUserId: Int,
    val roomId: Int,
    val status: InvitationStatus
) {
    constructor(apiInvitation: ApiRoomInvitation) : this(
        inviteeUserId = apiInvitation.inviteeUserId,
        inviterUserId = apiInvitation.inviterUserId,
        roomId = apiInvitation.roomId,
        status = InvitationStatus.fromString(apiInvitation.status)
    )
}


