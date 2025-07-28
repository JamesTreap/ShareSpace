package com.example.sharespace.core.data.repository.dto.calendar

import com.example.sharespace.core.data.repository.dto.finance.ApiBill
import com.example.sharespace.core.data.repository.dto.tasks.ApiTask

data class ApiCalendarResponse(
    val bills: List<ApiBill>,
    val tasks: List<ApiTask>
)