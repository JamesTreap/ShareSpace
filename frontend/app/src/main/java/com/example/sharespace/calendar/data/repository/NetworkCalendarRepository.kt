package com.example.sharespace.calendar.data.repository

import android.util.Log // Import Log
import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.CalendarRepository
import com.example.sharespace.core.data.repository.dto.calendar.ApiCalendarRequest
import com.example.sharespace.core.data.repository.dto.calendar.ApiCalendarResponse
import retrofit2.HttpException
import java.io.IOException // For network errors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NetworkCalendarRepository(private val apiService: ApiService) : CalendarRepository {

    companion object { // Define TAG for logging
        private const val TAG = "NetworkCalendarRepo"
    }

    override suspend fun getCalendarData(
        token: String,
        roomId: Int,
        date: LocalDate
    ): ApiCalendarResponse {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val request = ApiCalendarRequest(date = dateString)

        Log.d(TAG, "Fetching calendar data for roomId: $roomId, date: $dateString, request: $request")

        try {
            val response = apiService.getCalendarData(
                roomId = roomId,
                token = "Bearer $token", // Consider logging only a part of the token or its presence for security
                request = request
            )

            Log.d(TAG, "Raw response received. Code: ${response.code()}, Message: ${response.message()}")

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Log.i(TAG, "Successfully fetched calendar data. Response body: $responseBody")
                    return responseBody
                } else {
                    Log.e(TAG, "API response body was null for getCalendarData. Code: ${response.code()}")
                    throw IllegalStateException("API response body was null for getCalendarData. Code: ${response.code()}")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to fetch calendar data. Code: ${response.code()}, Error: $errorBody")
                // Throwing HttpException will allow ViewModel to catch it and extract details if needed
                throw HttpException(response)
            }
        } catch (e: HttpException) {
            // This will re-throw the HttpException from the 'else' block above
            // Or catch other HttpExceptions that might occur before the explicit check
            Log.e(TAG, "HttpException fetching calendar data: ${e.message()}", e)
            throw e // Re-throw
        } catch (e: IOException) {
            // For network errors (e.g., no internet connection)
            Log.e(TAG, "IOException fetching calendar data: ${e.message}", e)
            throw e // Re-throw
        } catch (e: Exception) {
            // For any other unexpected errors during the API call or processing
            Log.e(TAG, "Generic exception fetching calendar data: ${e.message}", e)
            throw e // Re-throw
        }
    }
}
