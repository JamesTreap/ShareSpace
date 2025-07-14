package com.example.sharespace.data.remote

import com.google.gson.annotations.SerializedName
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

data class ApiTask(
    val id: Int,
    val title: String,
    val description: String,
    val deadline: String,
    val statuses: Map<String, String>
)

data class CreateTaskRequest(
    val title: String,
    val date: String,
    val description: String,
    val assignees: List<Assignee>,
    val frequency: String,
    val repeat: String
)

data class Assignee(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("status")
    val status: String
)


data class UserDetails(
    val id: Int,
    val name: String,
    val profile_picture_url: String?,
    val username: String
)

data class CreateTaskResponse(
    val message: String
)
data class RoomMembersResponse(
    val roommates: List<UserDetails>
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

    @GET("tasks/list/{roomId}")
    suspend fun getTasksForRoom(
        @Path("roomId") roomId: Int,
        @Header("Authorization") token: String
    ): Response<List<ApiTask>>

    @GET("users/user_details/{userId}")
    suspend fun getUserDetailsById(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Response<ApiUser>

    @POST("tasks/create_task/{room_id}")
    suspend fun createTask(
        @Path("room_id") roomId: Int,
        @Body request: CreateTaskRequest,
        @Header("Authorization") token: String
    ): Response<CreateTaskResponse>


    @GET("rooms/{room_id}/members")
    suspend fun getRoomMembers(
        @Path("room_id") roomId: Int,
        @Header("Authorization") token: String
    ): Response<RoomMembersResponse>




}
