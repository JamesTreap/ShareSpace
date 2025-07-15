package com.example.sharespace.core.data.repository.implementation

import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.Result
import com.example.sharespace.core.data.repository.RoomRepository
import com.example.sharespace.core.data.repository.dto.ApiRoom
import com.example.sharespace.core.data.repository.dto.ApiRoomInvitation
import com.example.sharespace.core.data.repository.dto.ApiUser
import com.example.sharespace.core.data.repository.dto.CreateRoomRequest
import com.example.sharespace.core.data.repository.dto.InviteUserToRoomRequest
import com.example.sharespace.core.data.repository.dto.RespondToRoomInviteRequest
import com.example.sharespace.core.data.repository.dto.ApiRespondToRoomInviteResponse
import retrofit2.HttpException
import java.io.IOException


class RoomRepositoryImpl(private val apiService: ApiService) : RoomRepository {

    /**
     * Helper function to wrap API calls and convert responses/exceptions to the Result type.
     */
    private suspend fun <T : Any> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Success(apiCall.invoke())
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = "API Error ${e.code()}" + (if (!errorBody.isNullOrBlank()) ": $errorBody" else "")
            Result.Error(e, message)
        } catch (e: IOException) { // For network errors (no internet, server unreachable)
            Result.Error(e, "Network error: Please check your connection and try again.")
        } catch (e: Exception) { // For other unexpected errors (e.g., JSON parsing issues if response doesn't match DTO)
            Result.Error(e, "An unexpected error occurred: ${e.message}")
        }
    }

    override suspend fun getRoomsAndUserInvitations(token: String): Result<Pair<List<ApiRoom>, List<ApiRoomInvitation>>> {
        return safeApiCall {
            val response = apiService.getRoomsAndInvites("Bearer $token")
            if (response.isSuccessful) {
                val body = response.body() ?: throw IllegalStateException("API response body was null for getRoomsAndInvites")
                Pair(body.joinedRooms, body.roomInvitations)
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getJoinedRooms(token: String): Result<List<ApiRoom>> {
        return safeApiCall {
            val response = apiService.getJoinedRooms("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.joinedRooms ?: emptyList()
            } else {
                throw HttpException(response)
            }
        }
    }

//    override suspend fun getPendingRoomInvitations(token: String): Result<List<ApiRoomInvitation>> {
//        return safeApiCall {
//            // This assumes `apiService.getRoomInvites()` is designed to return Response<List<ApiRoomInvitation>>
//            // or a Response whose body directly contains List<ApiRoomInvitation> or can be mapped to it.
//            // If the /rooms/invites endpoint (as per some earlier JSON) returns List<ApiRoom> for rooms the user is invited to,
//            // then the `apiService.getRoomInvites()` signature and this method's return type would need adjustment.
//            val response = apiService.getRoomInvites("Bearer $token")
//            if (response.isSuccessful) {
//                response.body()
//            } else {
//                throw HttpException(response)
//            }
//        }
//    }

    override suspend fun getRoomDetails(token: String, roomId: Int): Result<ApiRoom> {
        return safeApiCall {
            val response = apiService.getRoomInfo(roomId = roomId, token = "Bearer $token")
            if (response.isSuccessful) {
                response.body() ?: throw IllegalStateException("API response body was null for getRoomDetails")
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getRoomMembers(token: String, roomId: Int): Result<List<ApiUser>> {
        return safeApiCall {
            val response = apiService.getRoomMembers(roomId = roomId, token = "Bearer $token")
            if (response.isSuccessful) {
                response.body()?.roommates ?: emptyList()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun createRoom(token: String, roomName: String): Result<ApiRoom> {
        return safeApiCall {
            val request = CreateRoomRequest(name = roomName)
            val response = apiService.createRoom("Bearer $token", request)
            if (response.isSuccessful) {
                response.body() ?: throw IllegalStateException("API response body was null for createRoom")
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun inviteUserToRoom(token: String, roomId: Int, inviteeUsername: String): Result<ApiRoomInvitation> {
        return safeApiCall {
            val request = InviteUserToRoomRequest(inviteeUsername = inviteeUsername)
            val response = apiService.inviteUserToRoom("Bearer $token", roomId, request)
            if (response.isSuccessful) {
                response.body() ?: throw IllegalStateException("API response body was null for inviteUserToRoom")
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun respondToRoomInvite(
        token: String,
        roomIdFromInvite: Int,
        status: String
    ): Result<ApiRespondToRoomInviteResponse> {
        return safeApiCall {
            val request = RespondToRoomInviteRequest(status = status)
            val response = apiService.respondToRoomInvite("Bearer $token", roomIdFromInvite, request)
            if (response.isSuccessful) {
                response.body() ?: throw IllegalStateException("API response body was null for respondToRoomInvite")
            } else {
                throw HttpException(response)
            }
        }
    }
}
