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
import com.example.sharespace.core.data.repository.CalendarRepository
import com.example.sharespace.core.data.repository.FinanceRepository
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

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

sealed class CalendarUiState {
    object Loading : CalendarUiState()
    data class Success(val tasks: List<Task>, val bills: List<Bill>) : CalendarUiState()
    data class Error(val message: String) : CalendarUiState()
}

// Ensure this matches the definition in your RecentBillsSection
// If it's from com.example.sharespace.finance.viewmodel.BillsUiState,
// ensure that definition is:
sealed interface BillsUiState { // This definition should be the single source of truth
    data object Loading : BillsUiState
    data class Error(val message: String) : BillsUiState
    data object Empty : BillsUiState
    data class Success(val bills: List<Bill>) : BillsUiState
}

class RoomSummaryViewModel(
    private val roomRepository: RoomRepository,
    private val userSessionRepository: UserSessionRepository,
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val financeRepository: FinanceRepository,
) : ViewModel() {

    // Properties for UI states
    var roomDetailsUiState: RoomDetailsUiState by mutableStateOf(RoomDetailsUiState.Loading)
        private set

    var roommatesUiState: RoomSummaryRoommatesUiState by mutableStateOf(RoomSummaryRoommatesUiState.Loading)
        private set

    var tasksUiState: TasksUiState by mutableStateOf(TasksUiState.Loading) // Start as Loading
        private set

    var billsUiState: BillsUiState by mutableStateOf(BillsUiState.Loading) // Already added by you
        private set

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedRoommates = MutableStateFlow<Set<Int>>(emptySet())
    val selectedRoommates: StateFlow<Set<Int>> = _selectedRoommates.asStateFlow()

    private val _calendarUiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val calendarUiState: StateFlow<CalendarUiState> = _calendarUiState.asStateFlow()

    val currentUserIdString: StateFlow<String?> = userSessionRepository.currentUserIdFlow
        .map { userIdInt -> userIdInt?.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    // Exposed currentUserId as Int StateFlow for BillsLazyRow compatibility
    val currentUserIdInt: StateFlow<Int?> = userSessionRepository.currentUserIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    private var currentRoomIdInternal: Int? = null

    init {
        viewModelScope.launch {
            roomDetailsUiState = RoomDetailsUiState.Loading
            roommatesUiState = RoomSummaryRoommatesUiState.Loading
            tasksUiState = TasksUiState.Loading
            billsUiState = BillsUiState.Loading // Initialize bills state to Loading

            val roomId = userSessionRepository.activeRoomIdFlow.first { it != null }
            currentRoomIdInternal = roomId
            Log.i(TAG, "Active Room ID received: $roomId")

            // Wait for currentUserIdInt for bill-related operations if they need Int
            val userIdInt = currentUserIdInt.first { it != null } // Using Int version for bills
            Log.i(TAG, "Current User ID (Int) received: $userIdInt")

            // Wait for currentUserIdString for other operations
            val userIdString = currentUserIdString.first { it != null }
            Log.i(TAG, "Current User ID (String) received: $userIdString")


            if (roomId != null && userIdInt != null && userIdString != null) {
                fetchRoomDetails()
                fetchRoomMembers()
                fetchTasks() // Uses userIdString implicitly via currentUserIdString.value
                fetchCalendarData() // Uses _selectedDate, token, roomId
                fetchBillsForSummary() // Add call to fetch bills
            } else {
                Log.e(TAG, "Critical error: RoomID or UserID is null after waiting. RoomId: $roomId, UserIdInt: $userIdInt, UserIdString: $userIdString")
                roomDetailsUiState = RoomDetailsUiState.Error
                roommatesUiState = RoomSummaryRoommatesUiState.Error
                tasksUiState = TasksUiState.Error
                billsUiState = BillsUiState.Error("User or room info missing.") // Set bills error state
            }
        }
    }

    fun fetchRoomDetails() {
        val roomId = currentRoomIdInternal ?: run {
            Log.w(TAG, "Cannot fetch room details, Room ID not yet available.")
            roomDetailsUiState = RoomDetailsUiState.Error
            return
        }
        viewModelScope.launch {
            roomDetailsUiState = RoomDetailsUiState.Loading
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    roomDetailsUiState = RoomDetailsUiState.Error; return@launch
                }
                val apiRoomDetails = roomRepository.getRoomDetails(token = token, roomId = roomId)
                roomDetailsUiState = RoomDetailsUiState.Success(Room(apiRoomDetails))
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
                    roommatesUiState = RoomSummaryRoommatesUiState.Error; return@launch
                }
                val apiMembers = roomRepository.getRoomMembers(token = token, roomId = roomId)
                roommatesUiState = RoomSummaryRoommatesUiState.Success(apiMembers.map { User(it) })
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

//    fun fetchBillsForSummary() {
//        val roomId = currentRoomIdInternal ?: run {
//            Log.w(TAG, "Cannot fetch bills, Room ID not yet available.")
//            billsUiState = BillsUiState.Error("Room ID not available.")
//            return
//        }
//        // currentUserIdInt is used by RecentBillsSection for filtering logic with bills.
//        // The fetch itself might not need userId, but the display logic does.
//        if (currentUserIdInt.value == null) {
//            Log.w(TAG, "Cannot fetch bills for display, User ID (Int) not yet available.")
//            billsUiState = BillsUiState.Loading // Or Error, depending on whether it's recoverable
//            return
//        }
//
//        viewModelScope.launch {
//            billsUiState = BillsUiState.Loading
//            try {
//                val token = userSessionRepository.userTokenFlow.first()
//                if (token == null) {
//                    Log.w(TAG, "No token available. Cannot fetch bills.")
//                    billsUiState = BillsUiState.Error("Authentication required.")
//                    return@launch
//                }
//
//                // Use the dedicated getBillList method from FinanceRepository
//                val apiBillDtos = financeRepository.getBillList(token, roomId)
//
//                // Map ApiBill DTOs to domain Bill model
//                val domainBills = apiBillDtos.map { apiBillDto -> Bill(apiBillDto) }
//
//                billsUiState = if (domainBills.isEmpty()) {
//                    BillsUiState.Empty
//                } else {
//                    BillsUiState.Success(domainBills)
//                }
//                Log.i(TAG, "Bills for summary fetched successfully using getBillList for roomId: $roomId, Count: ${domainBills.size}")
//
//            } catch (e: HttpException) {
//                val errorBody = e.response()?.errorBody()?.string()
//                Log.e(TAG, "HTTP error fetching bills: ${e.code()}, $errorBody", e)
//                billsUiState = BillsUiState.Error("Network error: ${e.code()}")
//            } catch (e: IOException) {
//                Log.e(TAG, "Network error fetching bills", e)
//                billsUiState = BillsUiState.Error("Network connection issue.")
//            } catch (e: Exception) {
//                Log.e(TAG, "Error fetching bills", e)
//                billsUiState = BillsUiState.Error(e.message ?: "Unknown error fetching bills.")
//            }
//        }
//    }

    // ... (existing imports)
// import com.example.sharespace.core.domain.model.Bill // Make sure Bill is imported

// ... (inside your RoomSummaryViewModel class)

    fun fetchBillsForSummary() {
        val roomId = currentRoomIdInternal ?: run {
            Log.w(TAG, "Cannot fetch bills, Room ID not yet available.")
            billsUiState = BillsUiState.Error("Room ID not available.") // Kept your original assignment
            return
        }
        // currentUserIdInt is used by RecentBillsSection for filtering logic with bills.
        // The fetch itself might not need userId, but the display logic does.
        if (currentUserIdInt.value == null) {
            Log.w(TAG, "Cannot fetch bills for display, User ID (Int) not yet available.")
            billsUiState = BillsUiState.Loading // Kept your original assignment (Or Error, depending on whether it's recoverable)
            return
        }

        viewModelScope.launch {
            billsUiState = BillsUiState.Loading // Kept your original assignment
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    Log.w(TAG, "No token available. Cannot fetch bills.")
                    billsUiState = BillsUiState.Error("Authentication required.") // Kept your original assignment
                    return@launch
                }

                Log.d(TAG, "Fetching bills for summary for roomId: $roomId") // Logging
                val apiBillDtos = financeRepository.getBillList(token, roomId)
                Log.d(TAG, "Received ${apiBillDtos.size} ApiBill DTOs from repository.") // Logging

                // Map ApiBill DTOs to domain Bill model
                val domainBills = apiBillDtos.map { apiBillDto ->
                    // Optional: Log each DTO before mapping if you suspect mapping issues
                    // Log.v(TAG, "Mapping ApiBill DTO: $apiBillDto")
                    Bill(apiBillDto)
                }

                // *** ADDED LOGGING FOR EACH BILL ***
                if (domainBills.isNotEmpty()) {
                    Log.i(TAG, "Successfully mapped ${domainBills.size} bills. Details for roomId: $roomId:")
                    domainBills.forEachIndexed { index, bill ->
                        // Using bill.toString(). Override toString() in your Bill, BillMetadata,
                        // and UserDueAmount domain models for more readable output.
                        Log.d(TAG, "Bill ${index + 1}: $bill")

                        // Example of more detailed manual logging if toString() isn't customized:
                        // Log.d(TAG, "Bill ${index + 1}: id=${bill.id}, title='${bill.title}', amount=${bill.amount}, payerUserId=${bill.payerUserId}, metadataUsersCount=${bill.metadata?.users?.size ?: 0}")
                    }
                } else {
                    Log.i(TAG, "No bills found or mapped for roomId: $roomId.")
                }
                // *** END OF ADDED LOGGING ***

                billsUiState = if (domainBills.isEmpty()) { // Kept your original assignment
                    BillsUiState.Empty
                } else {
                    BillsUiState.Success(domainBills)
                }
                Log.i(TAG, "Bills for summary fetched successfully using getBillList for roomId: $roomId, Count: ${domainBills.size}")

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "HTTP error fetching bills: ${e.code()}, $errorBody", e)
                billsUiState = BillsUiState.Error("Network error: ${e.code()}") // Kept your original assignment
            } catch (e: IOException) {
                Log.e(TAG, "Network error fetching bills", e)
                billsUiState = BillsUiState.Error("Network connection issue.") // Kept your original assignment
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching bills", e)
                billsUiState = BillsUiState.Error(e.message ?: "Unknown error fetching bills.") // Kept your original assignment
            }
        }
    }


    fun updateTaskStatus(taskToUpdate: Task, newIndividualStatus: String) {
        val currentLoggedInUserId = currentUserIdString.value ?: run {
            Log.w(TAG, "Cannot update task status, current user ID (String) is not available.")
            return
        }
        val currentTasksState = tasksUiState
        val tasksBeforeUpdate = if (currentTasksState is TasksUiState.Success) currentTasksState.tasks else {
            Log.w(TAG, "Cannot update task, current tasks state is not Success: $currentTasksState")
            return
        }

        viewModelScope.launch {
            val token = userSessionRepository.userTokenFlow.first() ?: run {
                Log.w(TAG, "Cannot update task, missing token.")
                return@launch
            }

            val modifiedStatusesMap = taskToUpdate.statuses.toMutableMap()
            if (modifiedStatusesMap[currentLoggedInUserId] == newIndividualStatus) {
                Log.d(TAG, "No change in status for user $currentLoggedInUserId, task ${taskToUpdate.id}. Skipping update.")
                return@launch
            }
            modifiedStatusesMap[currentLoggedInUserId] = newIndividualStatus
            val optimisticallyUpdatedTask = taskToUpdate.copy(statuses = modifiedStatusesMap)
            tasksUiState = TasksUiState.Success(tasksBeforeUpdate.map { if (it.id == optimisticallyUpdatedTask.id) optimisticallyUpdatedTask else it })

            try {
                val requestBody = ApiUpdateTaskRequest(
                    title = taskToUpdate.title, date = taskToUpdate.deadline,
                    description = taskToUpdate.description,
                    assignees = modifiedStatusesMap.map { (userId, status) -> ApiAssignee(userId, status.lowercase()) }
                )
                taskRepository.updateTask(token, taskToUpdate.id, requestBody)
                Log.i(TAG, "Task ${taskToUpdate.id} status update for user $currentLoggedInUserId successful.")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating task ${taskToUpdate.id} on backend", e)
                tasksUiState = TasksUiState.Success(tasksBeforeUpdate) // Revert
            }
        }
    }

    fun fetchCalendarData() {
        Log.d(TAG, "fetchCalendarData called. Current selectedDate: ${_selectedDate.value}")
        viewModelScope.launch {
            _calendarUiState.value = CalendarUiState.Loading
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    _calendarUiState.value = CalendarUiState.Error("Authentication token not found."); return@launch
                }
                val roomId = userSessionRepository.activeRoomIdFlow.first()
                if (roomId == null) {
                    _calendarUiState.value = CalendarUiState.Error("Active room not found."); return@launch
                }
                val calendarData = calendarRepository.getCalendarData(token, roomId, _selectedDate.value)
                _calendarUiState.value = CalendarUiState.Success(
                    tasks = calendarData.tasks.map { Task(it) },
                    bills = calendarData.bills.map { Bill(it) }
                )
            } catch (e: Exception) { // Simplified error handling for brevity
                Log.e(TAG, "Error in fetchCalendarData: ${e.message}", e)
                _calendarUiState.value = CalendarUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        fetchCalendarData()
    }

    fun updateSelectedRoommates(roommates: Set<Int>) {
        _selectedRoommates.value = roommates
        // Potentially re-filter calendar data or other relevant data if needed
    }

    companion object {
        private const val TAG = "RoomSummaryViewModel"
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)
                RoomSummaryViewModel(
                    application.container.roomRepository,
                    application.container.userSessionRepository,
                    application.container.taskRepository,
                    application.container.calendarRepository,
                    application.container.financeRepository
                )
            }
        }
    }
}
