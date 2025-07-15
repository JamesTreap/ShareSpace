package com.example.sharespace.core.data.remote

import com.example.sharespace.core.data.repository.dto.ApiJoinedRoomsResponse
import com.example.sharespace.core.data.repository.dto.ApiRespondToRoomInviteResponse
import com.example.sharespace.core.data.repository.dto.ApiRoom
import com.example.sharespace.core.data.repository.dto.ApiRoomInvitation
import com.example.sharespace.core.data.repository.dto.ApiRoomInvitesResponse
import com.example.sharespace.core.data.repository.dto.ApiRoomMembersResponse
import com.example.sharespace.core.data.repository.dto.ApiRoomsAndInvitationsResponse
import com.example.sharespace.core.data.repository.dto.ApiTask
import com.example.sharespace.core.data.repository.dto.ApiUser
import com.example.sharespace.core.data.repository.dto.CreateRoomRequest
import com.example.sharespace.core.data.repository.dto.InviteUserToRoomRequest
import com.example.sharespace.core.data.repository.dto.RespondToRoomInviteRequest
import com.google.gson.annotations.SerializedName
import retrofit2.http.*
import retrofit2.Response

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)




data class RoomsAndInvitesResponse(
    val joinedRooms: List<ApiRoom>,
    val roomInvitations: List<ApiRoomInvitation>
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

data class CreateTaskResponse(
    val message: String
)


interface ApiService {

    // --- Auth ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

//    @POST("auth/create-user")
//    suspend fun createAccount(
//        @Body request: CreateAccountRequest
//    ): Response<CreateAccountResponse>

    // --- Users ---
    @GET("users/user_details") // This seems to be for the currently authenticated user
    suspend fun getCurrentUserDetails( // Renamed for clarity from your existing `getUserDetails`
        @Header("Authorization") token: String
    ): Response<ApiUser>

    @GET("users/user_details/{userId}") // This is for getting any user by ID
    suspend fun getUserDetailsById(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Response<ApiUser>

//    @PATCH("users/update_profile")
//    suspend fun updateProfile(
//        @Header("Authorization") token: String,
//        @Body request: UpdateProfileRequest
//    ): Response<UpdateProfileResponse>

    // --- Rooms ---
    @GET("rooms/rooms-and-invitations")
    suspend fun getRoomsAndInvites(
        @Header("Authorization") token: String
    ): Response<ApiRoomsAndInvitationsResponse>

    @GET("rooms")
    suspend fun getJoinedRooms(
        @Header("Authorization") token: String
    ): Response<ApiJoinedRoomsResponse>

    @GET("rooms/invites")
    suspend fun getRoomInvites(
        @Header("Authorization") token: String
    ): Response<ApiRoomInvitesResponse>

    @GET("rooms/{roomId}/members")
    suspend fun getRoomMembers(
        @Path("roomId") roomId: Int,
        @Header("Authorization") token: String
    ): Response<ApiRoomMembersResponse>

    @GET("rooms/{roomId}")
    suspend fun getRoomInfo(
        @Path("roomId") roomId: Int,
        @Header("Authorization") token: String
    ): Response<ApiRoom> // Returns a single ApiRoom

    @POST("rooms/create")
    suspend fun createRoom(
        @Header("Authorization") token: String,
        @Body request: CreateRoomRequest
    ): Response<ApiRoom>

    @POST("rooms/{room_id}/invite")
    suspend fun inviteUserToRoom(
        @Header("Authorization") token: String,
        @Path("room_id") roomId: Int,
        @Body request: InviteUserToRoomRequest
    ): Response<ApiRoomInvitation>

    @POST("rooms/invites/{room_id}/respond")
    suspend fun respondToRoomInvite(
        @Header("Authorization") token: String,
        @Path("room_id") roomIdFromInvite: Int,
        @Body request: RespondToRoomInviteRequest
    ): Response<ApiRespondToRoomInviteResponse>


    // --- Tasks ---
    @GET("tasks/list/{roomId}")
    suspend fun getTasksForRoom(
        @Path("roomId") roomId: Int,
        @Header("Authorization") token: String
    ): Response<List<ApiTask>>

    @POST("tasks/create_task/{roomId}")
    suspend fun createTask(
        @Path("roomId") roomId: Int,
        @Header("Authorization") token: String,
        @Body request: CreateTaskRequest
    ): Response<CreateTaskResponse>

//    @PATCH("tasks/{task_id}")
//    suspend fun updateTask(
//        @Path("task_id") taskId: Int,
//        @Header("Authorization") token: String,
//        @Body request: UpdateTaskRequest
//    ): Response<UpdateTaskResponse>
//
//    // --- Finance ---
//    @GET("finance/transaction_list/{room_id}")
//    suspend fun getTransactionList(
//        @Path("room_id") roomId: Int,
//        @Header("Authorization") token: String
//    ): Response<TransactionListResponse>
//
//    @POST("finance/create_bill/{room_id}")
//    suspend fun createBill(
//        @Path("room_id") roomId: Int,
//        @Header("Authorization") token: String,
//        @Body request: CreateBillRequest // TODO: Define CreateBillRequest & Response
//    ): Response<CreateBillResponse>
//
//    @POST("finance/pay_user/{room_id}")
//    suspend fun payUser(
//        @Path("room_id") roomId: Int,
//        @Header("Authorization") token: String,
//        @Body request: PayUserRequest // TODO: Define PayUserRequest & Response
//    ): Response<PayUserResponse>
}
