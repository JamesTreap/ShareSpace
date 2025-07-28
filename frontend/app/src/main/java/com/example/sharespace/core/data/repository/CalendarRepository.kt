package com.example.sharespace.core.data.repository

import com.example.sharespace.core.data.repository.dto.calendar.ApiCalendarResponse
import java.time.LocalDate

interface CalendarRepository {
    /**
     * Fetches calendar data (tasks and bills) for a specific date in a room.
     * @param token The authorization token.
     * @param roomId The ID of the room for which to fetch calendar data.
     * @param date The date for which to fetch calendar data.
     * @return The calendar data containing tasks and bills.
     * @throws IllegalStateException if the HTTP call is successful (2xx) but the response body is null.
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues or other I/O problems during the request.
     */
    suspend fun getCalendarData(token: String, roomId: Int, date: LocalDate): ApiCalendarResponse
}