package com.example.sharespace.core.data.repository
import com.example.sharespace.core.data.repository.dto.ApiRespondToRoomInviteResponse
import com.example.sharespace.core.data.repository.dto.ApiRoom
import com.example.sharespace.core.data.repository.dto.ApiRoomInvitation
import com.example.sharespace.core.data.repository.dto.ApiUser

interface RoomRepository {

    /**
     * Fetches both rooms the user has joined and their pending room invitations.
     * @return A Pair containing a list of joined rooms and a list of pending invitations.
     * @throws IllegalStateException if the HTTP call is successful (2xx) but the response body is null.
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues or other I/O problems during the request.
     */
    suspend fun getRoomsAndUserInvitations(token: String): Pair<List<ApiRoom>, List<ApiRoomInvitation>>

    /**
     * Fetches only the rooms the current user has joined.
     * @return A list of rooms the user has joined.
     * @throws IllegalStateException if the HTTP call is successful (2xx) but the response body is null (and the DTO implies non-null data).
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues or other I/O problems during the request.
     */
    suspend fun getJoinedRooms(token: String): List<ApiRoom>

    /**
     * Fetches detailed information for a specific room.
     * @param roomId The ID of the room to fetch.
     * @return The detailed room information.
     * @throws IllegalStateException if the HTTP call is successful (2xx) but the response body is null.
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues or other I/O problems during the request.
     */
    suspend fun getRoomDetails(token: String, roomId: Int): ApiRoom

    /**
     * Fetches the list of members for a specific room.
     * @param roomId The ID of the room whose members are to be fetched.
     * @return A list of users who are members of the room.
     * @throws IllegalStateException if the HTTP call is successful (2xx) but the response body is null (and the DTO implies non-null data).
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues or other I/O problems during the request.
     */
    suspend fun getRoomMembers(token: String, roomId: Int): List<ApiUser>

    /**
     * Creates a new room.
     * @param roomName The name for the new room.
     * @return The created room details as ApiRoom.
     * @throws IllegalStateException if the HTTP call is successful (2xx) but the response body is null.
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues or other I/O problems during the request.
     */
    suspend fun createRoom(token: String, roomName: String): ApiRoom

    /**
     * Invites a user to a specific room.
     * @param roomId The ID of the room to invite the user to.
     * @param inviteeUsername The username of the user to invite.
     * @return The details of the created invitation as ApiRoomInvitation.
     * @throws IllegalStateException if the HTTP call is successful (2xx) but the response body is null.
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues or other I/O problems during the request.
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
     * @return Response indicating the outcome of the action.
     * @throws IllegalStateException if the HTTP call is successful (2xx) but the response body is null.
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues or other I/O problems during the request.
     */
    suspend fun respondToRoomInvite(
        token: String,
        roomIdFromInvite: Int,
        status: String
    ): ApiRespondToRoomInviteResponse
}
