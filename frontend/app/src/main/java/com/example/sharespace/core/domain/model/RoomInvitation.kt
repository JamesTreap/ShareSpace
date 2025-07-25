package com.example.sharespace.core.domain.model

import com.example.sharespace.core.data.repository.dto.rooms.ApiRoomInvitation


/**
 * Represents a room invitation in the domain layer.
 *
 * This class models the essential information about an invitation
 * that the application uses internally. It can be mapped from an
 * ApiRoomInvitation DTO via its secondary constructor.
 */
data class RoomInvitation(
    val inviteeUserId: Int,
    val inviterUserId: Int,
    val roomId: Int,
    val status: InvitationStatus // Using an enum for status for type safety and clarity
) {
    /**
     * Secondary constructor to create a RoomInvitation domain model
     * from an ApiRoomInvitation DTO.
     */
    constructor(apiInvitation: ApiRoomInvitation) : this(
        inviteeUserId = apiInvitation.inviteeUserId,
        inviterUserId = apiInvitation.inviterUserId,
        roomId = apiInvitation.roomId,
        status = InvitationStatus.fromString(apiInvitation.status)
    )
}


