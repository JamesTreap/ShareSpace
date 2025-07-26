package com.example.sharespace.room.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sharespace.ShareSpaceApplication
import com.example.sharespace.core.data.repository.RoomRepository
import com.example.sharespace.core.data.repository.TaskRepository
import com.example.sharespace.core.data.repository.UserSessionRepository
import com.example.sharespace.core.data.repository.dto.tasks.ApiAssignee
import com.example.sharespace.core.data.repository.dto.tasks.ApiUpdateTaskRequest
import com.example.sharespace.core.data.repository.dto.tasks.ApiUpdateTaskResponse
import com.example.sharespace.core.domain.model.Bill
import com.example.sharespace.core.domain.model.Room
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.core.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Sealed Interface for Room Details
sealed interface RoomDetailsUiState {
    data class Success(val roomDetails: Room) : RoomDetailsUiState
    object Error : RoomDetailsUiState
    object Loading : RoomDetailsUiState
}

// Roommates UI State
sealed interface RoomSummaryRoommatesUiState {
    data class Success(val roommates: List<User>) : RoomSummaryRoommatesUiState
    object Error : RoomSummaryRoommatesUiState
    object Loading : RoomSummaryRoommatesUiState
}

// Sealed Interface for Tasks UI State
sealed interface TasksUiState {
    data class Success(val tasks: List<Task>) : TasksUiState
    object Error : TasksUiState
    object Loading : TasksUiState
    object Empty : TasksUiState
}

class RoomSummaryViewModel(
    private val roomRepository: RoomRepository,
    private val userSessionRepository: UserSessionRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    // Properties for UI states
    var roomDetailsUiState: RoomDetailsUiState by mutableStateOf(RoomDetailsUiState.Loading)
        private set

    var roommatesUiState: RoomSummaryRoommatesUiState by mutableStateOf(RoomSummaryRoommatesUiState.Loading)
        private set

    var tasksUiState: TasksUiState by mutableStateOf(TasksUiState.Loading)
        private set


    // StateFlows for bills and tasks
    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private var currentRoomId: Int? = null

    init {
        viewModelScope.launch {
            currentRoomId = userSessionRepository.activeRoomIdFlow.first()
            if (currentRoomId != null) {
                fetchRoomDetails()
                fetchRoomMembers()
                fetchTasks()
            } else {
                Log.w(TAG, "No active room ID available on init.")
                roomDetailsUiState = RoomDetailsUiState.Error
                roommatesUiState = RoomSummaryRoommatesUiState.Error
                tasksUiState = TasksUiState.Error
            }
        }
    }

    fun fetchRoomDetails() {
        viewModelScope.launch {
            roomDetailsUiState = RoomDetailsUiState.Loading
            val roomId = currentRoomId
            if (roomId == null) {
                Log.w(TAG, "Cannot fetch room details, no active room ID.")
                roomDetailsUiState = RoomDetailsUiState.Error
                return@launch
            }
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    Log.w(TAG, "No token available. Cannot fetch room details.")
                    roomDetailsUiState = RoomDetailsUiState.Error
                    return@launch
                }

                // 1. Fetch ApiRoom from the repository
                val apiRoomDetails = roomRepository.getRoomDetails(token = token, roomId = roomId)

                // 2. Map ApiRoom to your domain Room using the secondary constructor
                val domainRoomDetails = Room(apiRoomDetails)

                // 3. Update UI state with the domain Room model
                roomDetailsUiState = RoomDetailsUiState.Success(domainRoomDetails)
                Log.i(TAG, "Room details fetched and mapped successfully for roomId: $roomId")

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching room details for roomId: $roomId", e)
                roomDetailsUiState = RoomDetailsUiState.Error
            }
        }
    }

    fun fetchRoomMembers() {
        viewModelScope.launch {
            roommatesUiState = RoomSummaryRoommatesUiState.Loading
            val roomId = currentRoomId
            if (roomId == null) {
                Log.w(TAG, "Cannot fetch room members, no active room ID.")
                roommatesUiState = RoomSummaryRoommatesUiState.Error
                return@launch
            }
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    Log.w(TAG, "No token available. Cannot fetch room members.")
                    roommatesUiState = RoomSummaryRoommatesUiState.Error
                    return@launch
                }
                val apiMembers = roomRepository.getRoomMembers(token = token, roomId = roomId)
                val domainMembers = apiMembers.map { apiUser -> User(apiUser) }
                roommatesUiState = RoomSummaryRoommatesUiState.Success(domainMembers)
                Log.i(
                    TAG,
                    "Room members fetched successfully for roomId: $roomId, Count: ${apiMembers.size}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching room members for roomId: $roomId", e)
                roommatesUiState = RoomSummaryRoommatesUiState.Error
            }
        }
    }

    fun fetchTasks() {
        viewModelScope.launch {
            tasksUiState = TasksUiState.Loading
            val roomId = currentRoomId
            if (roomId == null) {
                Log.w(TAG, "Cannot fetch tasks, no active room ID.")
                tasksUiState = TasksUiState.Error
                return@launch
            }
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    Log.w(TAG, "No token available. Cannot fetch tasks.")
                    tasksUiState = TasksUiState.Error
                    return@launch
                }
                // 1. Fetch List<ApiTask> from the repository
                val apiTasksList = taskRepository.getTasksForRoom(token = token, roomId = roomId)

                // 2. Map List<ApiTask> to List<Task> using the secondary constructor
                val domainTasksList = apiTasksList.map { apiTask -> Task(apiTask) }

                if (domainTasksList.isEmpty()) {
                    tasksUiState = TasksUiState.Empty
                } else {
                    tasksUiState = TasksUiState.Success(domainTasksList)
                }
                Log.i(
                    TAG,
                    "Tasks fetched successfully for roomId: $roomId, Count: ${domainTasksList.size}"
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks for roomId: $roomId", e)
                tasksUiState = TasksUiState.Error
            }
        }
    }

    fun updateTaskStatus(
        taskToUpdate: Task, newIndividualStatus: String
    ) {
        viewModelScope.launch {
            val tasksBeforeThisUpdate: List<Task>
            val currentTasksState = tasksUiState
            if (currentTasksState is TasksUiState.Success) {
                tasksBeforeThisUpdate = currentTasksState.tasks
            } else {
                Log.w(
                    TAG,
                    "Cannot update task, current tasks state is not Success: $currentTasksState"
                )
                return@launch
            }
            val userIdToUpdate = userSessionRepository.currentUserIdFlow.first()?.toString() ?: ""
            val token = userSessionRepository.userTokenFlow.first()
            if (token == null) {
                Log.w(TAG, "Cannot update task, missing token.")
                return@launch
            }

            // --- Optimistic UI Update ---
            val modifiedStatusesMap = taskToUpdate.statuses.toMutableMap()
            if (modifiedStatusesMap[userIdToUpdate] == newIndividualStatus) {
                Log.d(
                    TAG,
                    "No change in status for user $userIdToUpdate, task ${taskToUpdate.id}. Skipping update."
                )
                return@launch
            }
            modifiedStatusesMap[userIdToUpdate] = newIndividualStatus
            val optimisticallyUpdatedTask = taskToUpdate.copy(statuses = modifiedStatusesMap)
            val updatedTasksList = tasksBeforeThisUpdate.map { task ->
                if (task.id == optimisticallyUpdatedTask.id) optimisticallyUpdatedTask else task
            }
            tasksUiState = TasksUiState.Success(updatedTasksList)
            Log.d(
                TAG,
                "Optimistically updated task ${taskToUpdate.id} for user $userIdToUpdate to $newIndividualStatus"
            )
            // --- End Optimistic UI Update ---

            try {
                val finalApiAssigneesList = modifiedStatusesMap.map { (userId, status) ->
                    ApiAssignee(userId = userId, status = status.toLowerCase())
                }
                val requestBody = ApiUpdateTaskRequest(
                    title = taskToUpdate.title,
                    date = taskToUpdate.deadline,
                    description = taskToUpdate.description,
                    assignees = finalApiAssigneesList
                )

                Log.d(
                    TAG,
                    "Updating task ${taskToUpdate.id} on backend for user $userIdToUpdate. Request: $requestBody"
                )

                // This call now directly returns ApiUpdateTaskResponse or throws an exception
                val successResponse: ApiUpdateTaskResponse = taskRepository.updateTask(
                    token = token, taskId = taskToUpdate.id, request = requestBody
                )

                // If we reach here, the call was successful (no exception thrown)
                Log.i(
                    TAG,
                    "Task ${taskToUpdate.id} status update for user $userIdToUpdate successful: ${successResponse.message}"
                )
            } catch (e: HttpException) { // Catch specific HTTP errors from Retrofit/OkHttp
                val errorBody = e.response()?.errorBody()?.string() ?: "Unknown HTTP error"
                val statusCode = e.code()
                Log.e(
                    TAG,
                    "HTTP error updating task ${taskToUpdate.id} for user $userIdToUpdate on backend. Code: $statusCode, Body: $errorBody",
                    e
                )
                tasksUiState = TasksUiState.Success(tasksBeforeThisUpdate) // Revert
                // TODO: Show specific error message to the user
            } catch (e: IOException) { // Catch network errors (e.g., no internet)
                Log.e(
                    TAG,
                    "Network error updating task ${taskToUpdate.id} for user $userIdToUpdate on backend: ${e.message}",
                    e
                )
                tasksUiState = TasksUiState.Success(tasksBeforeThisUpdate) // Revert
                // TODO: Show specific error message to the user
            } catch (e: IllegalStateException) { // As per your repository's KDoc
                Log.e(
                    TAG,
                    "IllegalStateException updating task ${taskToUpdate.id} (e.g. null body when not expected): ${e.message}",
                    e
                )
                tasksUiState = TasksUiState.Success(tasksBeforeThisUpdate) // Revert
            } catch (e: Exception) { // Catch any other unexpected errors
                Log.e(
                    TAG,
                    "Generic exception updating task ${taskToUpdate.id} for user $userIdToUpdate on backend: ${e.message}",
                    e
                )
                tasksUiState = TasksUiState.Success(tasksBeforeThisUpdate) // Revert
                // TODO: Consider showing an error message to the user.
            }
        }
    }

    // Single companion object for TAG and Factory
    companion object {
        private const val TAG = "RoomSummaryViewModel" // TAG for logging

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)
                val roomRepository = application.container.roomRepository
                val userSessionRepository = application.container.userSessionRepository
                val taskRepository = application.container.taskRepository
                RoomSummaryViewModel(
                    roomRepository = roomRepository,
                    userSessionRepository = userSessionRepository,
                    taskRepository = taskRepository
                )
            }
        }
    }
}
