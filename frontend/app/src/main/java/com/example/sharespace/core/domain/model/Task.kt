package com.example.sharespace.core.domain.model

import java.time.LocalDateTime

data class Task(
    val id: String,
    val title: String,
    val dueDate: LocalDateTime,
    val iconResId: Int? = null,
    val isDone: Boolean = false
)