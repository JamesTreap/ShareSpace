package com.example.sharespace.core.data.repository.network

import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.RoomRepository
import com.example.sharespace.core.data.repository.dto.rooms.ApiCreateRoomRequest
import com.example.sharespace.core.data.repository.dto.rooms.ApiInviteUserToRoomRequest
import com.example.sharespace.core.data.repository.dto.rooms.ApiRespondToRoomInviteRequest
import com.example.sharespace.core.data.repository.dto.rooms.ApiRespondToRoomInviteResponse
import com.example.sharespace.core.data.repository.dto.rooms.ApiRoom
import com.example.sharespace.core.data.repository.dto.rooms.ApiRoomInvitation
import com.example.sharespace.core.data.repository.dto.rooms.ApiUpdateRoomRequest
import com.example.sharespace.core.data.repository.dto.users.ApiUser
import retrofit2.HttpException

class NetworkRoomRepository(private val apiService: ApiService) : RoomRepository {
    override suspend fun getRoomsAndUserInvitations(token: String): Pair<List<ApiRoom>, List<ApiRoom>> {
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
            return response.body()?.joinedRooms ?: emptyList()
        } else {
            throw HttpException(response)
        }
    }

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
            return response.body()?.roommates ?: emptyList()
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun createRoom(token: String, roomName: String): ApiRoom {
        val request = ApiCreateRoomRequest(name = roomName)
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
        val request = ApiInviteUserToRoomRequest(inviteeUsername = inviteeUsername)
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
        val request = ApiRespondToRoomInviteRequest(status = status)
        val response = apiService.respondToRoomInvite("Bearer $token", roomIdFromInvite, request)
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for respondToRoomInvite")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun updateRoomDetails(
        token: String,
        roomId: Int,
        updateRequest: ApiUpdateRoomRequest
    ): ApiRoom {
        val response = apiService.updateRoomDetails(
            token = "Bearer $token",
            roomId = roomId,
            requestBody = updateRequest
        )
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for updateRoomDetails")
        } else {
            throw HttpException(response) // Propagates error
        }
    }

    override suspend fun createRoom(
        token: String,
        updateRequest: ApiCreateRoomRequest
    ): ApiRoom {
        val response = apiService.createRoom(
            token = "Bearer $token",
            request = updateRequest
        )
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for updateRoomDetails")
        } else {
            throw HttpException(response) // Propagates error
        }
    }
}

