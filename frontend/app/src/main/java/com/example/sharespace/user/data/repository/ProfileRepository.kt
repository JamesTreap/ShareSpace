package com.example.sharespace.user.data.repository

import com.example.sharespace.core.data.remote.ApiClient
import com.example.sharespace.core.data.repository.dto.ApiRoom
import com.example.sharespace.core.data.repository.dto.ApiRoomInvitation
import com.example.sharespace.core.data.repository.dto.ApiUser
import retrofit2.HttpException

class ProfileRepository {
    private val api = ApiClient.apiService

    suspend fun getUser(token: String): ApiUser {
        val response = api.getCurrentUserDetails("Bearer $token")
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        return response.body() ?: throw IllegalStateException("User body was null")
    }

    suspend fun getRoomsAndInvites(token: String): Pair<List<ApiRoom>, List<ApiRoomInvitation>> {
        val response = api.getRoomsAndInvites("Bearer $token")
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        val body = response.body() ?: throw IllegalStateException("Rooms and invites body was null")
        return Pair(body.joinedRooms, body.roomInvitations)

    }
}
