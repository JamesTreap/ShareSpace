package com.example.sharespace.user.data.repository

import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.dto.rooms.ApiRespondToRoomInviteRequest
import com.example.sharespace.core.data.repository.dto.rooms.ApiRespondToRoomInviteResponse
import com.example.sharespace.core.data.repository.dto.rooms.ApiRoom
import com.example.sharespace.core.data.repository.dto.rooms.ApiRoomInvitation
import com.example.sharespace.core.data.repository.dto.users.ApiPatchProfileRequest
import com.example.sharespace.core.data.repository.dto.users.ApiUser
import retrofit2.HttpException

class ProfileRepository(var api: ApiService) {


    suspend fun getUser(token: String): ApiUser {
        val response = api.getCurrentUserDetails("Bearer $token")
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        return response.body() ?: throw IllegalStateException("User body was null")
    }

    suspend fun getRoomsAndInvites(token: String): Pair<List<ApiRoom>, List<ApiRoom>> {
        val response = api.getRoomsAndInvites("Bearer $token")
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        val body = response.body() ?: throw IllegalStateException("Rooms and invites body was null")
        return Pair(body.joinedRooms, body.roomInvitations)

    }

    suspend fun respondToRoomInvite(
        token: String,
        roomIdFromInvite: Int,
        status: String
    ): ApiRespondToRoomInviteResponse {
        val request = ApiRespondToRoomInviteRequest(status = status)
        val response = api.respondToRoomInvite("Bearer $token", roomIdFromInvite, request)
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for respondToRoomInvite")
        } else {
            throw HttpException(response)
        }
    }
    suspend fun patchProfile(
        token: String?,
        name: String,
        username: String,
        profilePictureUrl: String
    ): ApiUser {
        val request = ApiPatchProfileRequest(name, username, profilePictureUrl)
        val response = api.patchUserProfile("Bearer $token", request)
        if (!response.isSuccessful) {
            throw Exception("Failed to patch profile: ${response.code()}")
        }
        return response.body() ?: throw Exception("Empty response body")
    }
}
