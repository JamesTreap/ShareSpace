package com.example.sharespace.core.data.repository.network

import android.util.Log
import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.UserRepository
import com.example.sharespace.core.data.repository.dto.finance.ApiDeleteResponse
import com.example.sharespace.core.data.repository.dto.users.ApiUser
import com.example.sharespace.core.data.repository.dto.users.ApiUserWithDebts

class NetworkUserRepository(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getCurrentUserDetails(token: String): ApiUser? {
        return try {
            val response = apiService.getCurrentUserDetails("Bearer $token")
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to get current user details: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user details", e)
            null
        }
    }

    override suspend fun getUserDetailsById(token: String, userId: Int): ApiUser? {
        return try {
            val response = apiService.getUserDetailsById(userId, "Bearer $token")
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to get user details by ID: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user details by ID", e)
            null
        }
    }

    override suspend fun getRoomMembersWithDebts(
        token: String,
        roomId: Int
    ): List<ApiUserWithDebts> {
        return try {
            val response = apiService.getRoomMembersWithDebts(roomId, "Bearer $token")
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e(TAG, "Failed to get room members with debts: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting room members with debts", e)
            emptyList()
        }
    }

    override suspend fun cleanupRoomDebts(token: String, roomId: Int): ApiDeleteResponse? {
        return try {
            val response = apiService.cleanupRoomDebts(roomId, "Bearer $token")
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to cleanup room debts: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up room debts", e)
            null
        }
    }

    companion object {
        private const val TAG = "NetworkUserRepository"
    }
}