package com.example.sharespace.calendar.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sharespace.ShareSpaceApplication
import com.example.sharespace.core.data.repository.CalendarRepository
import com.example.sharespace.core.data.repository.RoomRepository
import com.example.sharespace.core.data.repository.UserSessionRepository
import com.example.sharespace.core.domain.model.Bill
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.core.domain.model.User // Assuming you might need user info later
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface CalendarScreenUiState {
    object Loading : CalendarScreenUiState
    data class Success(
        val tasks: List<Task>, val bills: List<Bill>, val roommates: List<User> // Added roommates
    ) : CalendarScreenUiState

    data class Error(val message: String) : CalendarScreenUiState
}

class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
    private val userSessionRepository: UserSessionRepository,
    private val roomRepository: RoomRepository // Added
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _uiState = MutableStateFlow<CalendarScreenUiState>(CalendarScreenUiState.Loading)
    val uiState: StateFlow<CalendarScreenUiState> = _uiState.asStateFlow()

    private val _selectedRoommates = MutableStateFlow<Set<Int>>(emptySet()) // Added
    val selectedRoommates: StateFlow<Set<Int>> = _selectedRoommates.asStateFlow() // Added

    val currentUserIdString: StateFlow<String?> = userSessionRepository.currentUserIdFlow // Added
        .map { userIdInt -> userIdInt?.toString() }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    private var currentRoomIdInternal: Int? = null

    init {
        viewModelScope.launch {
            try {
                currentRoomIdInternal = userSessionRepository.activeRoomIdFlow.first { it != null }
                // Fetch initial data which now includes roommates
                fetchCalendarDataAndRoommates()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing CalendarViewModel: ${e.message}", e)
                _uiState.value = CalendarScreenUiState.Error("Failed to load initial room data.")
            }
        }
    }

    // Combined fetch function
    fun fetchCalendarDataAndRoommates() {
        val roomId = currentRoomIdInternal ?: run {
            Log.w(TAG, "Cannot fetch data, Room ID not available.")
            _uiState.value = CalendarScreenUiState.Error("Room information not available.")
            return
        }

        Log.d(
            TAG,
            "fetchCalendarDataAndRoommates called for date: ${_selectedDate.value} and roomId: $roomId"
        )
        viewModelScope.launch {
            _uiState.value = CalendarScreenUiState.Loading
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    _uiState.value = CalendarScreenUiState.Error("Authentication token not found.")
                    return@launch
                }

                // Fetch calendar data
                val calendarData =
                    calendarRepository.getCalendarData(token, roomId, _selectedDate.value)
                // Fetch roommates
                val apiMembers = roomRepository.getRoomMembers(token, roomId)
                val domainRoommates = apiMembers.map { User(it) }

                _uiState.value = CalendarScreenUiState.Success(
                    tasks = calendarData.tasks.map { Task(it) },
                    bills = calendarData.bills.map { Bill(it) },
                    roommates = domainRoommates
                )
                Log.i(
                    TAG,
                    "Calendar data and roommates fetched successfully for $roomId on ${_selectedDate.value}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in fetchCalendarDataAndRoommates: ${e.message}", e)
                _uiState.value =
                    CalendarScreenUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }


    fun updateSelectedDate(date: LocalDate) {
        if (_selectedDate.value != date) {
            _selectedDate.value = date
            // Fetch data for the new date, roommates list might not need to be refetched unless it changes frequently
            fetchCalendarDataAndRoommates()
        }
    }

    fun updateSelectedRoommates(roommateIds: Set<Int>) { // Renamed parameter for clarity
        _selectedRoommates.value = roommateIds
        // No need to refetch from network, filtering is client-side in the Composable
    }

    // Retry function to be called from UI
    fun retryDataFetch() {
        fetchCalendarDataAndRoommates()
    }

    companion object {
        private const val TAG = "CalendarViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)
                CalendarViewModel(
                    calendarRepository = application.container.calendarRepository,
                    userSessionRepository = application.container.userSessionRepository,
                    roomRepository = application.container.roomRepository // Added
                )
            }
        }
    }
}
