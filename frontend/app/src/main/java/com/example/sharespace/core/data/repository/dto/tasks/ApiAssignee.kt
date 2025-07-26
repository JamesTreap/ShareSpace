package com.example.sharespace.core.data.repository.dto.tasks

import com.google.gson.annotations.SerializedName

data class ApiAssignee(
    @SerializedName("user_id")
    val userId: String,
    val status: String
)
