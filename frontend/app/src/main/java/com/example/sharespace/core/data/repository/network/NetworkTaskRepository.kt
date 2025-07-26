package com.example.sharespace.core.data.repository.network

import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.TaskRepository
import com.example.sharespace.core.data.repository.dto.tasks.ApiCreateTaskRequest
import com.example.sharespace.core.data.repository.dto.tasks.ApiCreateTaskResponse
import com.example.sharespace.core.data.repository.dto.tasks.ApiTask
import com.example.sharespace.core.data.repository.dto.tasks.ApiUpdateTaskRequest
import com.example.sharespace.core.data.repository.dto.tasks.ApiUpdateTaskResponse
import retrofit2.HttpException


class NetworkTaskRepository(private val apiService: ApiService) : TaskRepository {
    override suspend fun getTasksForRoom(token: String, roomId: Int): List<ApiTask> {
        val response = apiService.getTasksForRoom(roomId = roomId, token = "Bearer $token")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun createTask(
        token: String, roomId: Int, request: ApiCreateTaskRequest
    ): ApiCreateTaskResponse {
        val response = apiService.createTask(
            roomId = roomId, token = "Bearer $token", request = request
        )
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for createTask")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun updateTask(
        token: String, taskId: Int, request: ApiUpdateTaskRequest
    ): ApiUpdateTaskResponse {
        val response = apiService.updateTask(
            taskId = taskId, token = "Bearer $token", request = request
        )
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for updateTask")
        } else {
            throw HttpException(response)
        }
    }
}
