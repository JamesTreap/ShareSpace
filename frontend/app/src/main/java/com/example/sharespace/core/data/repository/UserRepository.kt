package com.example.sharespace.core.data.repository

import com.example.sharespace.core.data.repository.dto.users.ApiUser
import com.example.sharespace.core.data.repository.dto.users.ApiUserWithDebts
import com.example.sharespace.core.data.repository.dto.finance.ApiDeleteResponse

interface UserRepository {
    suspend fun getCurrentUserDetails(token: String): ApiUser?
    suspend fun getUserDetailsById(token: String, userId: Int): ApiUser?
    suspend fun getRoomMembersWithDebts(token: String, roomId: Int): List<ApiUserWithDebts>
    suspend fun cleanupRoomDebts(token: String, roomId: Int): ApiDeleteResponse?
}