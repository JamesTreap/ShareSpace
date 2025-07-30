package com.example.sharespace.room.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sharespace.ShareSpaceApplication
import com.example.sharespace.core.data.repository.RoomRepository
import com.example.sharespace.core.data.repository.UserSessionRepository
import com.example.sharespace.core.data.repository.dto.rooms.ApiCreateRoomRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class CreateRoomUiState(
    val roomName: String = "",
    val description: String = "",
    val isCreatingRoom: Boolean = false,
    val isUpdating: Boolean = false,
    val roomCreateError: String? = null,
    val roomCreateSuccess: Boolean = false
)

class CreateRoomViewModel(
    private val roomRepository: RoomRepository,
    private val userSessionRepository: UserSessionRepository
): ViewModel() {
    var uiState by mutableStateOf(CreateRoomUiState())
        private set

    fun onRoomNameChange(newName: String) {
        uiState = uiState.copy(roomName = newName, roomCreateError = null, roomCreateSuccess = false)
    }

    fun onDescriptionChange(newDescription: String) {
        uiState =
            uiState.copy(description = newDescription, roomCreateError = null, roomCreateSuccess = false)
    }

    fun consumeUpdateError() {
        uiState = uiState.copy(roomCreateError = null)
    }

    fun consumeUpdateSuccess() {
        uiState = uiState.copy(roomCreateSuccess = false)
    }

    fun createRoom() {
        if (uiState.isUpdating) return

        val name = uiState.roomName.trim()
        val description = uiState.description.trim()
        val address = ""

        if (name.isBlank()) {
            uiState = uiState.copy(roomCreateError = "Room name cannot be empty.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isUpdating = true, roomCreateError = null, roomCreateSuccess = false)
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    uiState = uiState.copy(
                        isUpdating = false, roomCreateError = "Authentication error. Please try again."
                    )
                    return@launch
                }

                // Construct the request object with all fields
                val updateRequest = ApiCreateRoomRequest(
                    name = name,
                    description = if (description.isNotEmpty()) description else null,
                    address = null
                )

                roomRepository.createRoom(
                    token = token, updateRequest = updateRequest
                )

                uiState = uiState.copy(isUpdating = false, roomCreateSuccess = true)

            } catch (e: HttpException) {
                uiState = uiState.copy(
                    isUpdating = false, roomCreateError = "Error creating room: ${e.message()}"
                )
            } catch (e: IOException) {
                uiState = uiState.copy(
                    isUpdating = false, roomCreateError = "Network error. Please check your connection."
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isUpdating = false,
                    roomCreateError = e.message ?: "Failed to update room. Please try again."
                )
            }
        }
    }

    companion object {
        private const val TAG = "CreateRoomViewModel"
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ShareSpaceApplication)
                val userSessionRepository = application.container.userSessionRepository
                val roomRepository = application.container.roomRepository
                CreateRoomViewModel(
                    roomRepository = roomRepository,
                    userSessionRepository = userSessionRepository
                )
            }
        }
    }
}