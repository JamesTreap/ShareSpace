package com.example.sharespace.core.data.repository.network

import com.example.sharespace.core.data.remote.ApiService
// Result class import is removed
import com.example.sharespace.core.data.repository.RoomRepository
import com.example.sharespace.core.data.repository.dto.ApiRespondToRoomInviteResponse
import com.example.sharespace.core.data.repository.dto.ApiRoom
import com.example.sharespace.core.data.repository.dto.ApiRoomInvitation
import com.example.sharespace.core.data.repository.dto.ApiUser
import com.example.sharespace.core.data.repository.dto.CreateRoomRequest
import com.example.sharespace.core.data.repository.dto.InviteUserToRoomRequest
import com.example.sharespace.core.data.repository.dto.RespondToRoomInviteRequest
import retrofit2.HttpException

// IOException import can remain as these exceptions might still be caught by the ViewModel
// import java.io.IOException

class NetworkRoomRepository(private val apiService: ApiService) : RoomRepository {

    // The safeApiCall helper function is removed.

    override suspend fun getRoomsAndUserInvitations(token: String): Pair<List<ApiRoom>, List<ApiRoomInvitation>> {
        val response = apiService.getRoomsAndInvites("Bearer $token")
        if (response.isSuccessful) {
            val body = response.body()
                ?: throw IllegalStateException("API response body was null for getRoomsAndInvites")
            return Pair(body.joinedRooms, body.roomInvitations)
        } else {
            throw HttpException(response) // Propagates error
        }
    }

    override suspend fun getJoinedRooms(token: String): List<ApiRoom> {
        val response = apiService.getJoinedRooms("Bearer $token")
        if (response.isSuccessful) {
            // Assuming joinedRooms in the DTO can be null, provide a default emptyList.
            // If it's non-nullable in your DTO, then `response.body()?.joinedRooms!!` or
            // handle a null body with an IllegalStateException as above.
            return response.body()?.joinedRooms ?: emptyList()
        } else {
            throw HttpException(response)
        }
    }

    // The getPendingRoomInvitations method was commented out, so I'll keep it that way.
    // If you uncomment it, apply the same pattern.

    override suspend fun getRoomDetails(token: String, roomId: Int): ApiRoom {
        val response = apiService.getRoomInfo(roomId = roomId, token = "Bearer $token")
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for getRoomDetails")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun getRoomMembers(token: String, roomId: Int): List<ApiUser> {
        val response = apiService.getRoomMembers(roomId = roomId, token = "Bearer $token")
        if (response.isSuccessful) {
            // Similar to getJoinedRooms, handle potential nullability based on your DTO.
            return response.body()?.roommates ?: emptyList()
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun createRoom(token: String, roomName: String): ApiRoom {
        val request = CreateRoomRequest(name = roomName)
        val response = apiService.createRoom("Bearer $token", request)
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for createRoom")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun inviteUserToRoom(
        token: String,
        roomId: Int,
        inviteeUsername: String
    ): ApiRoomInvitation {
        val request = InviteUserToRoomRequest(inviteeUsername = inviteeUsername)
        val response = apiService.inviteUserToRoom("Bearer $token", roomId, request)
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for inviteUserToRoom")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun respondToRoomInvite(
        token: String,
        roomIdFromInvite: Int,
        status: String
    ): ApiRespondToRoomInviteResponse {
        val request = RespondToRoomInviteRequest(status = status)
        val response = apiService.respondToRoomInvite("Bearer $token", roomIdFromInvite, request)
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for respondToRoomInvite")
        } else {
            throw HttpException(response)
        }
    }
}

