package com.example.sharespace.room.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sharespace.ShareSpaceApplication
import com.example.sharespace.core.data.repository.RoomRepository
import com.example.sharespace.core.data.repository.dto.ApiUser
import com.example.sharespace.core.domain.model.Bill
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.core.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

sealed interface RoomSummaryRoommatesUiState {
    data class Success(val roommates: List<ApiUser>) : RoomSummaryRoommatesUiState
    object Error : RoomSummaryRoommatesUiState // Simplified error state for now
    object Loading : RoomSummaryRoommatesUiState
}


@RequiresApi(Build.VERSION_CODES.O)
class RoomSummaryViewModel(
    private val roomRepository: RoomRepository
    // You could add other repositories here if needed
) : ViewModel() {

    /** The mutable State that stores the UI state for roommates */
    var roommatesUiState: RoomSummaryRoommatesUiState by mutableStateOf(RoomSummaryRoommatesUiState.Loading)
        private set

    // --- Keeping your existing StateFlows for bills and tasks for now ---
    // These could also be integrated into a larger RoomSummaryUiState if desired
    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()
    private val _roommates = MutableStateFlow<List<User>>(emptyList())
    val roommates: StateFlow<List<User>> = _roommates.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        // Load sample data for bills and tasks as before
//        fetchRoommates()
        loadSampleBillsAndTasks()

        // You would trigger fetching roommates here if token/roomId are available
        // For demonstration, let's assume you have a way to get them.
        // If not available at init, the fetchRoommates method needs to be called from the UI.
        // Example: fetchRoommates("dummy_token", 0) // Replace with actual token/ID logic
    }

    /**
     * Fetches roommates from the repository and updates the UI state.
     * TODO: Determine how you get `token` and `roomId`.
     */
//    fun fetchRoommates(token: String, roomId: Int) {
//        viewModelScope.launch {
//            roommatesUiState = RoomSummaryRoommatesUiState.Loading
//            roommatesUiState = try {
//                // Assuming your repository's getRoomMembers directly throws on network error
//                // or returns a Result wrapper.
//                when (val result =
//                    roomRepository.getRoomMembers(token = "Bearer $token", roomId = roomId)) {
//                    is Result.Success -> RoomSummaryRoommatesUiState.Success(result.data)
//                    is Result.Error -> {
//                        // Log the exception for debugging
//                        // Log.e("RoomSummaryVM", "Error fetching roommates: ${result.exception}", result.exception)
//                        RoomSummaryRoommatesUiState.Error // You could pass result.message here too
//                    }
//                }
//            } catch (e: IOException) {
//                // This catch block is more relevant if roomRepository.getRoomMembers
//                // can directly throw IOException (e.g., not wrapped in your Result type)
//                // Log.e("RoomSummaryVM", "IOException fetching roommates: ${e.message}", e)
//                RoomSummaryRoommatesUiState.Error
//            } catch (e: HttpException) {
//                // Similar to IOException, for Retrofit HTTP errors
//                // Log.e("RoomSummaryVM", "HttpException fetching roommates: ${e.code()} - ${e.message()}", e)
//                RoomSummaryRoommatesUiState.Error
//            }
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.O)
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
        // TODO: Implement actual logic
    }

    fun toggleTaskDone(task: Task) {
        _tasks.value = _tasks.value.map {
            if (it.id == task.id) it.copy(isDone = !it.isDone) else it
        }
    }

    /**
     * Companion object to provide a Factory for creating the ViewModel.
     */
    companion object {
        // The APPLICATION_KEY is part of androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // Get the Application instance
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ShareSpaceApplication)
                // Access the RoomRepository from the application's DI container
                val roomRepository =
                    application.container.roomRepository // Assumes 'container' is your ShareSpaceAppContainer
                RoomSummaryViewModel(roomRepository = roomRepository)
            }
        }
    }
}
