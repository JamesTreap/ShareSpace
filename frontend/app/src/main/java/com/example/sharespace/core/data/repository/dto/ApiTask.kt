package com.example.sharespace.core.data.repository.dto

data class ApiTask(
    val id: Int,
    val title: String,
    val description: String,
    val deadline: String,
    val statuses: Map<String, String>
)