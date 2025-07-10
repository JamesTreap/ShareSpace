package com.example.sharespace.data.remote

import retrofit2.http.*
import retrofit2.Response

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)

data class ApiUser(
    val id: Int,
    val name: String,
    val username: String,
    val profilePictureUrl: String?
)

data class ApiRoom(
    val id: Int,
    val name: String,
    val pictureUrl: String?,
    val balanceDue: Float,
    val alerts: Int,
    val members: List<Map<String, Int>>
)

data class RoomInvite(
    val inviteeUserId: Int,
    val inviterUserId: Int,
    val roomId: Int,
    val status: String
)

data class RoomsAndInvitesResponse(
    val joinedRooms: List<ApiRoom>,
    val roomInvitations: List<RoomInvite>
)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("users/user_details")
    suspend fun getUserDetails(
        @Header("Authorization") token: String
    ): Response<ApiUser>

    @GET("rooms/rooms-and-invitations")
    suspend fun getRoomsAndInvites(
        @Header("Authorization") token: String
    ): Response<RoomsAndInvitesResponse>
}
