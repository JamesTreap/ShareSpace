package com.example.sharespace.room.viewmodel

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.isEmpty
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
import com.example.sharespace.core.data.repository.dto.tasks.ApiUpdateTaskRequest
import com.example.sharespace.core.domain.model.Bill
import com.example.sharespace.core.domain.model.Room
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.core.domain.model.User // User class now has the secondary constructor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

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
            tasksUiState = TasksUiState.Loading // Set loading state
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
                val apiTasks = taskRepository.getTasksForRoom(token = token, roomId = roomId)
                // *** IMPORTANT: Implement your ApiTask to Task mapping here ***
                val domainTasks = apiTasks.map { apiTask ->
                    Task(
                        id = apiTask.id.toString(),
                        title = apiTask.title,
                        description = apiTask.description,
                        dueDate = try { LocalDateTime.parse(apiTask.date) } catch (e: Exception) { LocalDateTime.now() },
                        isDone = apiTask.status == "complete",
                        assignees = emptyList() // Placeholder: Map assignees properly
                    )
                }

                if (domainTasks.isEmpty()) {
                    tasksUiState = TasksUiState.Empty // Set to Empty if no tasks returned
                } else {
                    tasksUiState = TasksUiState.Success(domainTasks)
                }
                Log.i(TAG, "Tasks fetched successfully for roomId: $roomId, Count: ${domainTasks.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks for roomId: $roomId", e)
                tasksUiState = TasksUiState.Error
            }
        }
    }

    fun updateTaskStatus(taskToUpdate: Task, newStatus: Boolean) {
        viewModelScope.launch {
            val currentTasksState = tasksUiState
            if (currentTasksState !is TasksUiState.Success) {
                Log.w(TAG, "Cannot update task, current tasks state is not Success.")
                return@launch // Don't proceed if not in a success state with tasks
            }

            val roomId = currentRoomId
            val token = userSessionRepository.userTokenFlow.first()

            if (roomId == null || token == null) {
                Log.w(TAG, "Cannot update task, missing roomId or token.")
                // Optionally, show error to user. UI won't change here if not Success state.
                return@launch
            }

            // Optimistically update UI
            val updatedTasks = currentTasksState.tasks.map { task ->
                if (task.id == taskToUpdate.id) task.copy(isDone = newStatus) else task
            }
            tasksUiState = TasksUiState.Success(updatedTasks) // Update with new list
            Log.d(TAG, "Optimistically updated task: ${taskToUpdate.title} to isDone = $newStatus")

            try {
                val updateRequest = ApiUpdateTaskRequest(
                    title = taskToUpdate.title,
                    date = taskToUpdate.dueDate.toString(),
                    description = taskToUpdate.description,
                    status = if (newStatus) "complete" else "todo",
                    assignees = null // Or map current assignees if needed by API
                )
                taskRepository.updateTask(token = token, taskId = taskToUpdate.id.toInt(), request = updateRequest)
                Log.i(TAG, "Task ${taskToUpdate.title} updated successfully on backend.")
                // Optional: Re-fetch to ensure consistency if optimistic update isn't trusted fully
                // fetchTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating task ${taskToUpdate.title} on backend", e)
                // Revert optimistic update on error
                val revertedTasks = currentTasksState.tasks.map { task ->
                    if (task.id == taskToUpdate.id) task.copy(isDone = !newStatus) else task // Revert the change
                }
                tasksUiState = TasksUiState.Success(revertedTasks) // Update UI back to original state for this task
                // Consider showing an error message to the user
            }
        }
    }

    // Single companion object for TAG and Factory
    companion object {
        private const val TAG = "RoomSummaryViewModel" // TAG for logging

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[APPLICATION_KEY] as ShareSpaceApplication)
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
