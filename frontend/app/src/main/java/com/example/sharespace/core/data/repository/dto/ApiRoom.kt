package com.example.sharespace.core.data.repository.dto

import com.google.gson.annotations.SerializedName

data class ApiRoom(
    val id: Int,
    val name: String,
    val pictureUrl: String?,
    val balanceDue: Float,
    val alerts: Int,
    val members: List<ApiRoomMember>
)