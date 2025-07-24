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
import com.example.sharespace.core.data.repository.UserSessionRepository
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

// Sealed Interface for Room Details (remains the same)
sealed interface RoomDetailsUiState {
    data class Success(val roomDetails: Room) : RoomDetailsUiState // Changed from ApiRoom to Room
    object Error : RoomDetailsUiState
    object Loading : RoomDetailsUiState
}

// Roommates UI State (remains the same)
sealed interface RoomSummaryRoommatesUiState {
    data class Success(val roommates: List<User>) : RoomSummaryRoommatesUiState
    object Error : RoomSummaryRoommatesUiState
    object Loading : RoomSummaryRoommatesUiState
}

class RoomSummaryViewModel(
    private val roomRepository: RoomRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    // Properties for UI states
    var roomDetailsUiState: RoomDetailsUiState by mutableStateOf(RoomDetailsUiState.Loading)
        private set

    var roommatesUiState: RoomSummaryRoommatesUiState by mutableStateOf(RoomSummaryRoommatesUiState.Loading)
        private set

    // StateFlows for bills and tasks
    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private var currentRoomId: Int? = null

    init {
        viewModelScope.launch {
            currentRoomId = userSessionRepository.activeRoomIdFlow.first()
            if (currentRoomId != null) {
                fetchRoomDetails()
                fetchRoomMembers()
            } else {
                Log.w(TAG, "No active room ID available on init.")
                roomDetailsUiState = RoomDetailsUiState.Error
                roommatesUiState = RoomSummaryRoommatesUiState.Error
            }
        }
        loadSampleBillsAndTasks()
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

    private fun loadSampleBillsAndTasks() {
        _bills.value = listOf(
            Bill(id = "b1", title = "Rent", amount = 100.0, subtitle = "Owing to Roommate 1"),
            Bill(id = "b2", title = "Hydro", amount = 50.0, subtitle = "Owing to Roommate 2")
        )
        _tasks.value = listOf(
            Task(
                id = "t1",
                title = "Take out garbage",
                dueDate = LocalDateTime.now().plusDays(1),
                isDone = false
            ),
            Task(
                id = "t2",
                title = "Clean bathroom",
                dueDate = LocalDateTime.now().plusDays(2),
                isDone = true
            )
        )
    }

    fun payBill(bill: Bill) {
        Log.d(TAG, "Pay bill: ${bill.title}")
    }

    fun toggleTaskDone(task: Task) {
        _tasks.value = _tasks.value.map {
            if (it.id == task.id) it.copy(isDone = !it.isDone) else it
        }
        Log.d(TAG, "Toggled task: ${task.title}")
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
                RoomSummaryViewModel(
                    roomRepository = roomRepository,
                    userSessionRepository = userSessionRepository
                )
            }
        }
    }
}
