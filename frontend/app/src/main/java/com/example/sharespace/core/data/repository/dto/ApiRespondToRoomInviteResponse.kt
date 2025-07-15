package com.example.sharespace.core.data.repository.dto

import androidx.compose.ui.graphics.Path
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

//placeholder
data class ApiRespondToRoomInviteResponse(
    val message: String?,
    val roomId: Int?,
    val newStatus: String?
)