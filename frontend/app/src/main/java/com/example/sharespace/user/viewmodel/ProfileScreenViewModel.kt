package com.example.sharespace.user.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sharespace.ShareSpaceApplication
import com.example.sharespace.core.data.repository.UserSessionRepository
import com.example.sharespace.core.domain.model.Room
import com.example.sharespace.core.domain.model.RoomInvitation
import com.example.sharespace.core.domain.model.User
import com.example.sharespace.user.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// UI States for different sections
sealed interface RoomsUiState {
    object Loading : RoomsUiState
    data class Success(val rooms: List<Room>) : RoomsUiState
    object Error : RoomsUiState
}

sealed interface InvitesUiState {
    object Loading : InvitesUiState
    data class Success(val invites: List<Room>) : InvitesUiState
    object Error : InvitesUiState
}

class ProfileScreenViewModel(
    private val userSessionRepository: UserSessionRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    // backing state
    private val _user = mutableStateOf<User?>(null)
    private val _roomsUiState = MutableStateFlow<RoomsUiState>(RoomsUiState.Loading)
    private val _invitesUiState = MutableStateFlow<InvitesUiState>(InvitesUiState.Loading)

    // public streams
    val user: MutableState<User?> = _user
    val roomsUiState: MutableStateFlow<RoomsUiState> = _roomsUiState
    val invitesUiState: MutableStateFlow<InvitesUiState> = _invitesUiState

    // Legacy properties for backward compatibility
    val rooms: MutableStateFlow<List<Room>> = MutableStateFlow(emptyList())
    val invites: MutableStateFlow<List<RoomInvitation>> = MutableStateFlow(emptyList())


    fun acceptInvite(roomId: Int) {
        viewModelScope.launch {
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) return@launch
                profileRepository.respondToRoomInvite(token, roomId, "accepted")
                loadData()
            } catch (e: Exception) {
                Log.e(TAG, "Error accepting invite for room $roomId", e)
            }
        }
    }

    fun declineInvite(roomId: Int) {
        viewModelScope.launch {
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) return@launch
                profileRepository.respondToRoomInvite(token, roomId, "rejected")
                loadData()
            } catch (e: Exception) {
                Log.e(TAG, "Error declining invite for room $roomId", e)
            }
        }
    }

    fun onProfileScreenEntered() {
        viewModelScope.launch {
            userSessionRepository.saveActiveRoomId(null)
//            Log.d(TAG, "Active room ID cleared on profile screen entry.")
        }
        loadData()
    }

    fun setActiveRoom(roomId: Int) {
        viewModelScope.launch {
            userSessionRepository.saveActiveRoomId(roomId)
//            Log.d(TAG, "Active room ID set to: $roomId")
        }
    }


    fun loadData() {
        viewModelScope.launch {
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    return@launch
                }
                // Log.d(TAG, "Loading data with token: $token")
                val apiUser = profileRepository.getUser(token)
                _user.value = User(
                    id = apiUser.id,
                    name = apiUser.name,
                    username = apiUser.username,
                    photoUrl = apiUser.profilePictureUrl
                )
                val (apiJoinedRooms, apiRoomInvites) = profileRepository.getRoomsAndInvites(token)
                _roomsUiState.value = RoomsUiState.Success(apiJoinedRooms.map { apiRoom ->
                    Room(apiRoom)
                })
                _invitesUiState.value = InvitesUiState.Success(apiRoomInvites.map { apiInvite ->
                    Room(apiInvite)
                })

            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile data", e)
                _roomsUiState.value = RoomsUiState.Error
                _invitesUiState.value = InvitesUiState.Error
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)
                val userSessionRepository = application.container.userSessionRepository
                val profileRepository = application.container.profileRepository
                ProfileScreenViewModel(
                    userSessionRepository = userSessionRepository,
                    profileRepository = profileRepository
                )
            }
        }
        private const val TAG = "ProfileScreenViewModel"
    }
}
