package com.example.sharespace.core.data.repository // Or your actual package

import com.example.sharespace.core.data.repository.dto.ApiRespondToRoomInviteResponse
import com.example.sharespace.core.data.repository.dto.ApiRoom
import com.example.sharespace.core.data.repository.dto.ApiRoomInvitation
import com.example.sharespace.core.data.repository.dto.ApiUser

// The Result class is removed.

interface RoomRepository {

    /**
     * Fetches both rooms the user has joined and their pending room invitations.
     * @throws Exception if the network request fails or an error occurs.
     */
    suspend fun getRoomsAndUserInvitations(token: String): Pair<List<ApiRoom>, List<ApiRoomInvitation>>

    /**
     * Fetches only the rooms the current user has joined.
     * @throws Exception if the network request fails or an error occurs.
     */
    suspend fun getJoinedRooms(token: String): List<ApiRoom>

    /**
     * Fetches detailed information for a specific room.
     * @throws Exception if the network request fails or an error occurs.
     */
    suspend fun getRoomDetails(token: String, roomId: Int): ApiRoom

    /**
     * Fetches the list of members for a specific room.
     * @throws Exception if the network request fails or an error occurs.
     */
    suspend fun getRoomMembers(token: String, roomId: Int): List<ApiUser>

    /**
     * Creates a new room.
     * @param roomName The name for the new room.
     * @return The created room details as ApiRoom.
     * @throws Exception if the network request fails or an error occurs.
     */
    suspend fun createRoom(token: String, roomName: String): ApiRoom

    /**
     * Invites a user to a specific room.
     * @param roomId The ID of the room to invite the user to.
     * @param inviteeUsername The username of the user to invite.
     * @return The details of the created invitation as ApiRoomInvitation.
     * @throws Exception if the network request fails or an error occurs.
     */
    suspend fun inviteUserToRoom(
        token: String,
        roomId: Int,
        inviteeUsername: String
    ): ApiRoomInvitation

    /**
     * Responds to a room invitation (accept or reject).
     * @param roomIdFromInvite The ID of the room from the invitation.
     * @param status The response status ("accepted" or "rejected").
     * @return Placeholder response. Replace with actual response DTO when known.
     * @throws Exception if the network request fails or an error occurs.
     */
    suspend fun respondToRoomInvite(
        token: String,
        roomIdFromInvite: Int,
        status: String
    ): ApiRespondToRoomInviteResponse
}
