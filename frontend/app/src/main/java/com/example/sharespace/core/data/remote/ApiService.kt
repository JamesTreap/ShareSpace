package com.example.sharespace.core.data.remote

import com.example.sharespace.core.data.repository.dto.auth.ApiCreateAccountRequest
import com.example.sharespace.core.data.repository.dto.auth.ApiCreateAccountResponse
import com.example.sharespace.core.data.repository.dto.auth.ApiLoginRequest
import com.example.sharespace.core.data.repository.dto.auth.ApiLoginResponse
import com.example.sharespace.core.data.repository.dto.rooms.ApiCreateRoomRequest
import com.example.sharespace.core.data.repository.dto.rooms.ApiInviteUserToRoomRequest
import com.example.sharespace.core.data.repository.dto.rooms.ApiJoinedRoomsResponse
import com.example.sharespace.core.data.repository.dto.rooms.ApiRespondToRoomInviteRequest
import com.example.sharespace.core.data.repository.dto.rooms.ApiRespondToRoomInviteResponse
import com.example.sharespace.core.data.repository.dto.rooms.ApiRoom
import com.example.sharespace.core.data.repository.dto.rooms.ApiRoomInvitation
import com.example.sharespace.core.data.repository.dto.rooms.ApiRoomInvitesResponse
import com.example.sharespace.core.data.repository.dto.rooms.ApiRoomMembersResponse
import com.example.sharespace.core.data.repository.dto.rooms.ApiRoomsAndInvitationsResponse
import com.example.sharespace.core.data.repository.dto.tasks.ApiCreateTaskRequest
import com.example.sharespace.core.data.repository.dto.tasks.ApiCreateTaskResponse
import com.example.sharespace.core.data.repository.dto.tasks.ApiTask
import com.example.sharespace.core.data.repository.dto.tasks.ApiUpdateTaskRequest
import com.example.sharespace.core.data.repository.dto.tasks.ApiUpdateTaskResponse
import com.example.sharespace.core.data.repository.dto.users.ApiPatchProfileRequest
import com.example.sharespace.core.data.repository.dto.users.ApiUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // --- Auth ---
    @POST("auth/login")
    suspend fun login(@Body request: ApiLoginRequest): Response<ApiLoginResponse>

    @POST("auth/create-user")
    suspend fun createAccount(
        @Body request: ApiCreateAccountRequest
    ): Response<ApiCreateAccountResponse>

    // --- Users ---
    @GET("users/user_details") // This seems to be for the currently authenticated user
    suspend fun getCurrentUserDetails( // Renamed for clarity from your existing `getUserDetails`
        @Header("Authorization") token: String
    ): Response<ApiUser>

    @GET("users/user_details/{userId}") // This is for getting any user by ID
    suspend fun getUserDetailsById(
        @Path("userId") userId: Int, @Header("Authorization") token: String
    ): Response<ApiUser>

    @PATCH("users/update_profile")
    suspend fun patchUserProfile(
        @Header("Authorization") token: String, @Body request: ApiPatchProfileRequest
    ): Response<ApiUser>

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
        @Path("roomId") roomId: Int, @Header("Authorization") token: String
    ): Response<ApiRoomMembersResponse>

    @GET("rooms/{roomId}")
    suspend fun getRoomInfo(
        @Path("roomId") roomId: Int, @Header("Authorization") token: String
    ): Response<ApiRoom> // Returns a single ApiRoom

    @POST("rooms/create")
    suspend fun createRoom(
        @Header("Authorization") token: String, @Body request: ApiCreateRoomRequest
    ): Response<ApiRoom>

    @POST("rooms/{room_id}/invite")
    suspend fun inviteUserToRoom(
        @Header("Authorization") token: String,
        @Path("room_id") roomId: Int,
        @Body request: ApiInviteUserToRoomRequest
    ): Response<ApiRoomInvitation>

    @POST("rooms/invites/{room_id}/respond")
    suspend fun respondToRoomInvite(
        @Header("Authorization") token: String,
        @Path("room_id") roomIdFromInvite: Int,
        @Body request: ApiRespondToRoomInviteRequest
    ): Response<ApiRespondToRoomInviteResponse>


    // --- Tasks ---
    @GET("tasks/list/{roomId}")
    suspend fun getTasksForRoom(
        @Path("roomId") roomId: Int, @Header("Authorization") token: String
    ): Response<List<ApiTask>>

    @POST("tasks/create_task/{roomId}")
    suspend fun createTask(
        @Path("roomId") roomId: Int,
        @Header("Authorization") token: String,
        @Body request: ApiCreateTaskRequest
    ): Response<ApiCreateTaskResponse>

    @PATCH("tasks/{task_id}")
    suspend fun updateTask(
        @Path("task_id") taskId: Int,
        @Header("Authorization") token: String,
        @Body request: ApiUpdateTaskRequest
    ): Response<ApiUpdateTaskResponse>

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
