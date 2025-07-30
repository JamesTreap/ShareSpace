package com.example.sharespace.core.data.repository.dto.rooms

import com.example.sharespace.core.data.repository.dto.users.ApiUser

data class ApiRoomMembersResponse(
    val roommates: List<ApiUser>
)
