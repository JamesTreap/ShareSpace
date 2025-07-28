package com.example.sharespace.calendar.data.repository

import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.CalendarRepository
import com.example.sharespace.core.data.repository.dto.calendar.ApiCalendarRequest
import com.example.sharespace.core.data.repository.dto.calendar.ApiCalendarResponse
import retrofit2.HttpException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NetworkCalendarRepository(private val apiService: ApiService) : CalendarRepository {

    override suspend fun getCalendarData(
        token: String,
        roomId: Int,
        date: LocalDate
    ): ApiCalendarResponse {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val request = ApiCalendarRequest(date = dateString)

        val response = apiService.getCalendarData(
            roomId = roomId,
            token = "Bearer $token",
            request = request
        )

        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for getCalendarData")
        } else {
            throw HttpException(response)
        }
    }
}