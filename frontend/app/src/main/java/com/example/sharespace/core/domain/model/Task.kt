package com.example.sharespace.core.domain.model

import com.example.sharespace.core.data.repository.dto.tasks.ApiTask

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val deadline: String,
    val statuses: Map<String, String>,
    val frequency: String?,
    val repeat: Int,
    val scheduledDate: String
) {
    constructor(apiTask: ApiTask) : this(
        id = apiTask.id,
        title = apiTask.title,
        description = apiTask.description,
        deadline = apiTask.deadline,
        statuses = apiTask.statuses,
        frequency = apiTask.frequency,
        repeat = apiTask.repeat,
        scheduledDate = apiTask.scheduledDate
    )
}
