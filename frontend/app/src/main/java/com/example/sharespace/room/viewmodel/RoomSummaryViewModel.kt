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
import com.example.sharespace.core.domain.model.Bill // Keep if bills are used
import com.example.sharespace.core.domain.model.Room
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.core.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    object Loading : TasksUiState // Initial state, also if waiting for dependencies
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

    var tasksUiState: TasksUiState by mutableStateOf(TasksUiState.Loading) // Start as Loading
        private set

    // StateFlows for bills (if needed)
    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    // Exposed currentUserId as a String StateFlow for the UI
    val currentUserIdString: StateFlow<String?> = userSessionRepository.currentUserIdFlow
        .map { userIdInt -> userIdInt?.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null // Initially null until the flow emits
        )

    private var currentRoomIdInternal: Int? = null // For internal ViewModel use

    init {
        viewModelScope.launch {
            // Set initial loading states for all sections
            roomDetailsUiState = RoomDetailsUiState.Loading
            roommatesUiState = RoomSummaryRoommatesUiState.Loading
            tasksUiState = TasksUiState.Loading

            // 1. Wait for a non-null activeRoomIdFlow
            val roomId = userSessionRepository.activeRoomIdFlow.first { it != null }
            currentRoomIdInternal = roomId // Store it for use in fetch functions
            Log.i(TAG, "Active Room ID received: $roomId")

            // 2. Wait for a non-null currentUserIdString
            //    currentUserIdString itself depends on userSessionRepository.currentUserIdFlow
            val userId = currentUserIdString.first { it != null }
            Log.i(TAG, "Current User ID received: $userId")

            // 3. Now that both critical IDs are available, proceed to fetch data
            //    If any of these fetches can proceed with only one ID, you can move them up.
            //    For this example, we assume all three need both (or at least roomID, and tasks need userID).
            if (roomId != null && userId != null) { // Double check, though `first { it != null }` should ensure this
                fetchRoomDetails()
                fetchRoomMembers()
                fetchTasks()
            } else {
                // This case should ideally not be reached if `first { it != null }` works as expected
                // and UserSessionRepository correctly provides these IDs.
                // If it is reached, it indicates a problem upstream (e.g., user not logged in, error in session repo)
                Log.e(TAG, "Critical error: RoomID or UserID is null after waiting. RoomId: $roomId, UserId: $userId")
                roomDetailsUiState = RoomDetailsUiState.Error
                roommatesUiState = RoomSummaryRoommatesUiState.Error
                tasksUiState = TasksUiState.Error
                // Consider navigating away or showing a global error message
            }
        }
    }

    fun fetchRoomDetails() {
        val roomId = currentRoomIdInternal ?: run {
            Log.w(TAG, "Cannot fetch room details, Room ID not yet available.")
            roomDetailsUiState = RoomDetailsUiState.Error // Or keep Loading if init is still running
            return
        }

        viewModelScope.launch {
            roomDetailsUiState = RoomDetailsUiState.Loading // Set loading for this specific fetch
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    Log.w(TAG, "No token available. Cannot fetch room details.")
                    roomDetailsUiState = RoomDetailsUiState.Error
                    return@launch
                }
                val apiRoomDetails = roomRepository.getRoomDetails(token = token, roomId = roomId)
                val domainRoomDetails = Room(apiRoomDetails)
                roomDetailsUiState = RoomDetailsUiState.Success(domainRoomDetails)
                Log.i(TAG, "Room details fetched successfully for roomId: $roomId")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching room details for roomId: $roomId", e)
                roomDetailsUiState = RoomDetailsUiState.Error
            }
        }
    }

    fun fetchRoomMembers() {
        val roomId = currentRoomIdInternal ?: run {
            Log.w(TAG, "Cannot fetch room members, Room ID not yet available.")
            roommatesUiState = RoomSummaryRoommatesUiState.Error
            return
        }

        viewModelScope.launch {
            roommatesUiState = RoomSummaryRoommatesUiState.Loading
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
                Log.i(TAG, "Room members fetched successfully for roomId: $roomId, Count: ${domainMembers.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching room members for roomId: $roomId", e)
                roommatesUiState = RoomSummaryRoommatesUiState.Error
            }
        }
    }

    fun fetchTasks() {
        val roomId = currentRoomIdInternal ?: run {
            Log.w(TAG, "Cannot fetch tasks, Room ID not yet available.")
            tasksUiState = TasksUiState.Error
            return
        }
        // Ensure currentUserIdString has emitted a non-null value for task operations.
        // The init block already waits for this, but manual retries should also be safe.
        if (currentUserIdString.value == null) {
            Log.w(TAG, "Cannot fetch tasks, User ID not yet available.")
            tasksUiState = TasksUiState.Loading // Or Error, if this state is unexpected after init
            return
        }

        viewModelScope.launch {
            tasksUiState = TasksUiState.Loading
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    Log.w(TAG, "No token available. Cannot fetch tasks.")
                    tasksUiState = TasksUiState.Error
                    return@launch
                }
                val apiTasksList = taskRepository.getTasksForRoom(token = token, roomId = roomId)
                val domainTasksList = apiTasksList.map { apiTask -> Task(apiTask) }

                tasksUiState = if (domainTasksList.isEmpty()) {
                    TasksUiState.Empty
                } else {
                    TasksUiState.Success(domainTasksList)
                }
                Log.i(TAG, "Tasks fetched successfully for roomId: $roomId, Count: ${domainTasksList.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks for roomId: $roomId", e)
                tasksUiState = TasksUiState.Error
            }
        }
    }

    fun updateTaskStatus(taskToUpdate: Task, newIndividualStatus: String) {
        val currentLoggedInUserId = currentUserIdString.value
        if (currentLoggedInUserId == null) {
            Log.w(TAG, "Cannot update task status, current user ID is not available.")
            // Optionally, show a transient error to the user
            return
        }

        val currentTasks = tasksUiState
        val tasksBeforeThisUpdate: List<Task> = if (currentTasks is TasksUiState.Success) {
            currentTasks.tasks
        } else {
            Log.w(TAG, "Cannot update task, current tasks state is not Success: $currentTasks")
            // Potentially show error or prevent update if tasks aren't even loaded
            return
        }

        viewModelScope.launch {
            val token = userSessionRepository.userTokenFlow.first()
            if (token == null) {
                Log.w(TAG, "Cannot update task, missing token.")
                return@launch
            }

            // --- Optimistic UI Update ---
            val modifiedStatusesMap = taskToUpdate.statuses.toMutableMap()
            if (modifiedStatusesMap[currentLoggedInUserId] == newIndividualStatus) {
                Log.d(TAG, "No change in status for user $currentLoggedInUserId, task ${taskToUpdate.id}. Skipping update.")
                return@launch
            }
            modifiedStatusesMap[currentLoggedInUserId] = newIndividualStatus
            val optimisticallyUpdatedTask = taskToUpdate.copy(statuses = modifiedStatusesMap)
            val updatedTasksList = tasksBeforeThisUpdate.map { task ->
                if (task.id == optimisticallyUpdatedTask.id) optimisticallyUpdatedTask else task
            }
            tasksUiState = TasksUiState.Success(updatedTasksList)
            Log.d(TAG, "Optimistically updated task ${taskToUpdate.id} for user $currentLoggedInUserId to $newIndividualStatus")
            // --- End Optimistic UI Update ---

            try {
                val finalApiAssigneesList = modifiedStatusesMap.map { (userId, status) ->
                    ApiAssignee(userId = userId, status = status.lowercase()) // Ensure lowercase for API
                }
                val requestBody = ApiUpdateTaskRequest(
                    title = taskToUpdate.title,
                    date = taskToUpdate.deadline,
                    description = taskToUpdate.description,
                    assignees = finalApiAssigneesList
                )

                Log.d(TAG, "Updating task ${taskToUpdate.id} on backend for user $currentLoggedInUserId. Request: $requestBody")
                val successResponse: ApiUpdateTaskResponse = taskRepository.updateTask(
                    token = token, taskId = taskToUpdate.id, request = requestBody
                )
                Log.i(TAG, "Task ${taskToUpdate.id} status update for user $currentLoggedInUserId successful: ${successResponse.message}")

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: "Unknown HTTP error"
                Log.e(TAG, "HTTP error updating task ${taskToUpdate.id}. Code: ${e.code()}, Body: $errorBody", e)
                tasksUiState = TasksUiState.Success(tasksBeforeThisUpdate) // Revert
            } catch (e: IOException) {
                Log.e(TAG, "Network error updating task ${taskToUpdate.id}: ${e.message}", e)
                tasksUiState = TasksUiState.Success(tasksBeforeThisUpdate) // Revert
            } catch (e: IllegalStateException) {
                Log.e(TAG, "IllegalStateException updating task ${taskToUpdate.id}: ${e.message}", e)
                tasksUiState = TasksUiState.Success(tasksBeforeThisUpdate) // Revert
            } catch (e: Exception) {
                Log.e(TAG, "Generic exception updating task ${taskToUpdate.id}: ${e.message}", e)
                tasksUiState = TasksUiState.Success(tasksBeforeThisUpdate) // Revert
            }
        }
    }

    companion object {
        private const val TAG = "RoomSummaryViewModel"

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
