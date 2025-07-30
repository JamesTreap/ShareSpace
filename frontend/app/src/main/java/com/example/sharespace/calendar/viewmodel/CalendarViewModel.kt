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
import com.example.sharespace.core.data.repository.UserSessionRepository
import com.example.sharespace.core.domain.model.Bill
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.core.domain.model.User // Assuming you might need user info later
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

// UI State for the Calendar Screen
sealed interface CalendarScreenUiState {
    object Loading : CalendarScreenUiState
    data class Success(
        val tasks: List<Task>,
        val bills: List<Bill>,
        // You might also want to include roommates here if filtering by user is needed directly on this screen
        // val roommates: List<User> = emptyList()
    ) : CalendarScreenUiState
    data class Error(val message: String) : CalendarScreenUiState
}

class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
    private val userSessionRepository: UserSessionRepository
    // Potentially RoomRepository if you need fresh roommate list specifically for this screen
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _uiState = MutableStateFlow<CalendarScreenUiState>(CalendarScreenUiState.Loading)
    val uiState: StateFlow<CalendarScreenUiState> = _uiState.asStateFlow()

    // If you need to filter by specific roommates on this dedicated calendar screen
    // private val _selectedRoommates = MutableStateFlow<Set<Int>>(emptySet())
    // val selectedRoommates: StateFlow<Set<Int>> = _selectedRoommates.asStateFlow()

    private var currentRoomIdInternal: Int? = null

    init {
        // Initialize and fetch data
        viewModelScope.launch {
            try {
                currentRoomIdInternal = userSessionRepository.activeRoomIdFlow.first { it != null }
                fetchCalendarData()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing CalendarViewModel: ${e.message}", e)
                _uiState.value = CalendarScreenUiState.Error("Failed to load initial room data.")
            }
        }
    }

    fun fetchCalendarData() {
        val roomId = currentRoomIdInternal ?: run {
            Log.w(TAG, "Cannot fetch calendar data, Room ID not available.")
            _uiState.value = CalendarScreenUiState.Error("Room information not available.")
            return
        }

        Log.d(TAG, "fetchCalendarData called for date: ${_selectedDate.value} and roomId: $roomId")
        viewModelScope.launch {
            _uiState.value = CalendarScreenUiState.Loading
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    _uiState.value = CalendarScreenUiState.Error("Authentication token not found.")
                    return@launch
                }

                val calendarData = calendarRepository.getCalendarData(token, roomId, _selectedDate.value)
                _uiState.value = CalendarScreenUiState.Success(
                    tasks = calendarData.tasks.map { Task(it) }, // Assuming Task constructor from ApiTask
                    bills = calendarData.bills.map { Bill(it) }   // Assuming Bill constructor from ApiBill
                )
                Log.i(TAG, "Calendar data fetched successfully for $roomId on ${_selectedDate.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error in fetchCalendarData: ${e.message}", e)
                _uiState.value = CalendarScreenUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun updateSelectedDate(date: LocalDate) {
        if (_selectedDate.value != date) {
            _selectedDate.value = date
            fetchCalendarData() // Re-fetch data for the new date
        }
    }

    // Example if you add roommate filtering
    // fun updateSelectedRoommates(roommates: Set<Int>) {
    //     _selectedRoommates.value = roommates
    //     fetchCalendarData() // Or apply a client-side filter if data is already fetched
    // }

    companion object {
        private const val TAG = "CalendarViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)
                CalendarViewModel(
                    calendarRepository = application.container.calendarRepository,
                    userSessionRepository = application.container.userSessionRepository
                    // roomRepository = application.container.roomRepository // if needed
                )
            }
        }
    }
}
