package com.example.sharespace.core.data.repository.dto.tasks

data class ApiUpdateTaskRequest(
    val title: String,
    val date: String,
    val description: String,
    val assignees: List<ApiAssignee>
)