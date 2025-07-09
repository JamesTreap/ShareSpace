package com.example.sharespace.data.repository

import com.example.sharespace.data.remote.ApiClient
import com.example.sharespace.data.remote.ApiUser
import com.example.sharespace.data.remote.ApiRoom
import com.example.sharespace.data.remote.RoomInvite
import retrofit2.HttpException

class ProfileRepository {
    private val api = ApiClient.apiService

    suspend fun getUser(token: String): ApiUser {
        val response = api.getUserDetails("Bearer $token")
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        return response.body() ?: throw IllegalStateException("User body was null")
    }

    suspend fun getRoomsAndInvites(token: String): Pair<List<ApiRoom>, List<RoomInvite>> {
        val response = api.getRoomsAndInvites("Bearer $token")
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        val body = response.body() ?: throw IllegalStateException("Rooms and invites body was null")
        return Pair(body.joinedRooms, body.roomInvitations)
    }
}
