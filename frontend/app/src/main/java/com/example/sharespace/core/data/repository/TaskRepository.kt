package com.example.sharespace.core.data.repository

import com.example.sharespace.core.data.repository.dto.tasks.ApiCreateTaskRequest
import com.example.sharespace.core.data.repository.dto.tasks.ApiCreateTaskResponse
import com.example.sharespace.core.data.repository.dto.tasks.ApiTask
import com.example.sharespace.core.data.repository.dto.tasks.ApiUpdateTaskRequest
import com.example.sharespace.core.data.repository.dto.tasks.ApiUpdateTaskResponse

interface TaskRepository {

    /**
     * Fetches all tasks for a specific room.
     * @param token The authorization token.
     * @param roomId The ID of the room for which to fetch tasks.
     * @return A list of tasks.
     * @throws IllegalStateException if the HTTP call is successful (2xx) but the response body is null (and the DTO implies non-null data).
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues or other I/O problems during the request.
     */
    suspend fun getTasksForRoom(token: String, roomId: Int): List<ApiTask>

    /**
     * Creates a new task in a specific room.
     * @param token The authorization token.
     * @param roomId The ID of the room where the task will be created.
     * @param request The details for the new task.
     * @return The created task details.
     * @throws IllegalStateException if the HTTP call is successful (2xx) but the response body is null.
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues or other I/O problems during the request.
     */
    suspend fun createTask(
        token: String, roomId: Int, request: ApiCreateTaskRequest
    ): ApiCreateTaskResponse

    /**
     * Updates an existing task.
     * @param token The authorization token.
     * @param taskId The ID of the task to update.
     * @param request The update details for the task.
     * @return Response indicating the outcome of the update action.
     * @throws IllegalStateException if the HTTP call is successful (2xx) but the response body is null.
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues or other I/O problems during the request.
     */
    suspend fun updateTask(
        token: String, taskId: Int, request: ApiUpdateTaskRequest
    ): ApiUpdateTaskResponse

}
