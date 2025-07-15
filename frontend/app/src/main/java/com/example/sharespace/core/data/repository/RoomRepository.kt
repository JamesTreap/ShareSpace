package com.example.sharespace.core.data.repository

import com.example.sharespace.core.data.repository.dto.ApiRespondToRoomInviteResponse
import com.example.sharespace.core.data.repository.dto.ApiRoom
import com.example.sharespace.core.data.repository.dto.ApiRoomInvitation
import com.example.sharespace.core.data.repository.dto.ApiUser


// Generic Result wrapper (can be in a more general location if used by other repositories)
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val message: String? = null) : Result<Nothing>()
    // object Loading : Result<Nothing>() // Optional, if you want to explicitly represent loading states
}

interface RoomRepository {

    /**
     * Fetches both rooms the user has joined and their pending room invitations.
     */
    suspend fun getRoomsAndUserInvitations(token: String): Result<Pair<List<ApiRoom>, List<ApiRoomInvitation>>>

    /**
     * Fetches only the rooms the current user has joined.
     */
    suspend fun getJoinedRooms(token: String): Result<List<ApiRoom>>

    /**
     * Fetches pending room invitations for the current user.
     * Note: This assumes an endpoint that returns a list of ApiRoomInvitation objects.
     * If /rooms/invites returns a list of ApiRoom that the user is invited to,
     * the return type should be Result<List<ApiRoom>>.
     */
//    suspend fun getPendingRoomInvitations(token: String): Result<List<ApiRoomInvitation>>

    /**
     * Fetches detailed information for a specific room.
     */
    suspend fun getRoomDetails(token: String, roomId: Int): Result<ApiRoom>

    /**
     * Fetches the list of members for a specific room.
     */
    suspend fun getRoomMembers(token: String, roomId: Int): Result<List<ApiUser>>

    /**
     * Creates a new room.
     * @param roomName The name for the new room.
     * @return The created room details as ApiRoom.
     */
    suspend fun createRoom(token: String, roomName: String): Result<ApiRoom>

    /**
     * Invites a user to a specific room.
     * @param roomId The ID of the room to invite the user to.
     * @param inviteeUsername The username of the user to invite.
     * @return The details of the created invitation as ApiRoomInvitation.
     */
    suspend fun inviteUserToRoom(
        token: String,
        roomId: Int,
        inviteeUsername: String
    ): Result<ApiRoomInvitation>

    /**
     * Responds to a room invitation (accept or reject).
     * @param roomIdFromInvite The ID of the room from the invitation.
     * @param status The response status ("accepted" or "rejected").
     * @return Placeholder response. Replace with actual response DTO when known.
     */
    suspend fun respondToRoomInvite(
        token: String,
        roomIdFromInvite: Int,
        status: String
    ): Result<ApiRespondToRoomInviteResponse>
}

