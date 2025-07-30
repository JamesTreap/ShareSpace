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
import com.example.sharespace.core.data.repository.dto.rooms.ApiRoom
import com.example.sharespace.core.data.repository.dto.rooms.ApiUpdateRoomRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class EditRoomUiState(
    val currentRoomDetails: ApiRoom? = null,
    val roomName: String = "",
    val description: String = "",
    val address: String = "",
    val isLoadingCurrentDetails: Boolean = true,
    val isUpdating: Boolean = false,
    val updateError: String? = null,
    val updateSuccess: Boolean = false,
    val initialLoadError: String? = null
)

class EditRoomViewModel(
    private val roomRepository: RoomRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    var uiState by mutableStateOf(EditRoomUiState())
        private set

    private var currentRoomId: Int? = null

    init {
        viewModelScope.launch {
            // Fetch active room ID from UserSessionRepository
            val roomIdFromSession =
                userSessionRepository.activeRoomIdFlow.first { it != null } // Wait for non-null
            if (roomIdFromSession == null) { // Should ideally not happen if we wait for non-null
                Log.e(TAG, "Active Room ID not available from session.")
                uiState = uiState.copy(
                    isLoadingCurrentDetails = false,
                    initialLoadError = "Active room information is missing. Cannot edit."
                )
                return@launch
            }
            currentRoomId = roomIdFromSession
            fetchCurrentRoomDetails(roomIdFromSession)
        }
    }

    private fun fetchCurrentRoomDetails(roomIdToFetch: Int) { // Renamed param to avoid confusion
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingCurrentDetails = true, initialLoadError = null)
            try {
                val token = userSessionRepository.userTokenFlow.first() // Wait for non-null
                if (token == null) {
                    uiState = uiState.copy(
                        isLoadingCurrentDetails = false,
                        initialLoadError = "Authentication error. Please log in."
                    )
                    return@launch
                }

                val roomDetails = roomRepository.getRoomDetails(token, roomIdToFetch)
                uiState = uiState.copy(
                    isLoadingCurrentDetails = false,
                    currentRoomDetails = roomDetails,
                    roomName = roomDetails.name,
                    description = roomDetails.description ?: "",
                    address = roomDetails.address ?: ""
                )
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error fetching room details for $roomIdToFetch", e)
                uiState = uiState.copy(
                    isLoadingCurrentDetails = false,
                    initialLoadError = "Error loading room details: ${e.message()}"
                )
            } catch (e: IOException) {
                Log.e(TAG, "Network error fetching room details for $roomIdToFetch", e)
                uiState = uiState.copy(
                    isLoadingCurrentDetails = false,
                    initialLoadError = "Network error. Please check your connection."
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching room details for $roomIdToFetch", e)
                uiState = uiState.copy(
                    isLoadingCurrentDetails = false,
                    initialLoadError = "Failed to load room details. Please try again."
                )
            }
        }
    }

    fun onRoomNameChange(newName: String) {
        uiState = uiState.copy(roomName = newName, updateError = null, updateSuccess = false)
    }

    fun onDescriptionChange(newDescription: String) {
        uiState =
            uiState.copy(description = newDescription, updateError = null, updateSuccess = false)
    }

    fun onAddressChange(newAddress: String) {
        uiState = uiState.copy(address = newAddress, updateError = null, updateSuccess = false)
    }

    fun saveRoomChanges() {
        val roomIdToUpdate = currentRoomId
        if (roomIdToUpdate == null) {
            uiState = uiState.copy(updateError = "Error: Active room ID is missing.")
            Log.w(TAG, "Attempted to save changes with no active room ID.")
            return
        }

        if (uiState.isUpdating) return

        val name = uiState.roomName.trim()
        val description = uiState.description.trim()
        val address = uiState.address.trim()

        if (name.isBlank()) {
            uiState = uiState.copy(updateError = "Room name cannot be empty.")
            return
        }

        val currentDetails = uiState.currentRoomDetails
        if (currentDetails != null && name == currentDetails.name && description == (currentDetails.description
                ?: "") && address == (currentDetails.address ?: "")
        ) {
            uiState = uiState.copy(updateError = "No changes detected.", updateSuccess = false)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isUpdating = true, updateError = null, updateSuccess = false)
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    uiState = uiState.copy(
                        isUpdating = false, updateError = "Authentication error. Please try again."
                    )
                    return@launch
                }

                val name = uiState.roomName.trim()
                val description = uiState.description.trim()
                val address = uiState.address.trim()

                // Construct the request object with all fields
                val updateRequest = ApiUpdateRoomRequest(
                    name = name,
                    description = if (description.isNotEmpty()) description else null,
                    address = if (address.isNotEmpty()) address else null
                )

                roomRepository.updateRoomDetails(
                    token = token, roomId = roomIdToUpdate, updateRequest = updateRequest
                )

                uiState = uiState.copy(isUpdating = false, updateSuccess = true)
                Log.i(TAG, "Room $roomIdToUpdate details updated successfully.")

            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error updating room $roomIdToUpdate", e)
                uiState = uiState.copy(
                    isUpdating = false, updateError = "Error updating room: ${e.message()}"
                )
            } catch (e: IOException) {
                Log.e(TAG, "Network error updating room $roomIdToUpdate", e)
                uiState = uiState.copy(
                    isUpdating = false, updateError = "Network error. Please check your connection."
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error updating room $roomIdToUpdate", e)
                uiState = uiState.copy(
                    isUpdating = false,
                    updateError = e.message ?: "Failed to update room. Please try again."
                )
            }
        }
    }

    fun consumeUpdateError() {
        uiState = uiState.copy(updateError = null)
    }

    fun consumeUpdateSuccess() {
        uiState = uiState.copy(updateSuccess = false)
    }

    companion object {
        private const val TAG = "EditRoomViewModel"
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ShareSpaceApplication)
                val roomRepository = application.container.roomRepository
                val userSessionRepository = application.container.userSessionRepository
                EditRoomViewModel(
                    roomRepository = roomRepository, userSessionRepository = userSessionRepository
                )
            }
        }
    }
}
