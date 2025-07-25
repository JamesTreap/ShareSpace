package com.example.sharespace.core.data.repository.dto.tasks

data class ApiTask(
    val id: Int,
    val title: String,
    val description: String,
    val deadline: String, // Primary due date for the task instance
    val statuses: Map<String, String>, // Key: User ID (as String), Value: Status (e.g., "TODO", "COMPLETED")
    val frequency: String?, // Nullable: e.g., "2w", "1d", "3m", or null
    val repeat: Int, // Non-nullable based on data (always 1 or more)
    val scheduledDate: String // Non-nullable based on data
)