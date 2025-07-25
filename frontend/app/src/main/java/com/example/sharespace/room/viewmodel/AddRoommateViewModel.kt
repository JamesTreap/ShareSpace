package com.example.sharespace.room.viewmodel // Or your preferred ViewModel package

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// UI State for AddRoommateScreen
data class AddRoommateUiState(
    val username: String = "",
    val isSendingInvite: Boolean = false,
    val inviteError: String? = null,
    val inviteSuccess: Boolean = false
)

class AddRoommateViewModel(
    private val roomRepository: RoomRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    var uiState by mutableStateOf(AddRoommateUiState())
        private set

    private var currentRoomId: Int? = null

    init {
        viewModelScope.launch {
            currentRoomId = userSessionRepository.activeRoomIdFlow.first()
            if (currentRoomId == null) {
                Log.w(TAG, "No active room ID available. Cannot send invites.")
                // You might want to reflect this error in uiState or disable the screen
                uiState = uiState.copy(inviteError = "Cannot send invite: No active room selected.")
            }
        }
    }

    fun onUsernameChange(newUsername: String) {
        uiState = uiState.copy(
            username = newUsername,
            inviteError = null, // Clear previous error on new input
            inviteSuccess = false // Reset success state
        )
    }

    fun clearUsername() {
        uiState = uiState.copy(
            username = "",
            inviteError = null,
            inviteSuccess = false
        )
    }

    fun sendInvite() {
        val targetUsername = uiState.username.trim()
        if (targetUsername.isBlank()) {
            uiState = uiState.copy(inviteError = "Username cannot be empty.")
            return
        }
        if (uiState.isSendingInvite) {
            return // Prevent multiple concurrent invites
        }

        val roomId = currentRoomId
        if (roomId == null) {
            uiState = uiState.copy(inviteError = "Error: No active room to invite to.")
            Log.w(TAG, "Attempted to send invite with no active room ID.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSendingInvite = true, inviteError = null, inviteSuccess = false)
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    Log.w(TAG, "No token available. Cannot send invite.")
                    uiState = uiState.copy(
                        isSendingInvite = false,
                        inviteError = "Authentication error. Please try again."
                    )
                    return@launch
                }

                // Call the repository function
                roomRepository.inviteUserToRoom(
                    token = token,
                    roomId = roomId,
                    inviteeUsername = targetUsername
                )

                // If successful
                uiState = uiState.copy(isSendingInvite = false, inviteSuccess = true, username = "") // Clear username on success
                Log.i(TAG, "Invite sent successfully to $targetUsername for room $roomId")

            } catch (e: Exception) { // Catch specific exceptions if needed (e.g., HttpException)
                Log.e(TAG, "Error sending invite to $targetUsername for room $roomId", e)
                uiState = uiState.copy(
                    isSendingInvite = false,
                    inviteError = e.message ?: "Failed to send invite. Please try again."
                )
            }
        }
    }

    // Companion object for TAG and Factory
    companion object {
        private const val TAG = "AddRoommateViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[APPLICATION_KEY] as ShareSpaceApplication)
                val roomRepository = application.container.roomRepository
                val userSessionRepository = application.container.userSessionRepository
                AddRoommateViewModel(
                    roomRepository = roomRepository,
                    userSessionRepository = userSessionRepository
                )
            }
        }
    }
}
