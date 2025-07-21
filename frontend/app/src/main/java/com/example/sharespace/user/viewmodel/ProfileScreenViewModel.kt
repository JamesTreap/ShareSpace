package com.example.sharespace.user.viewmodel

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
import com.example.sharespace.core.domain.model.User
import com.example.sharespace.ui.screens.profile.Room
import com.example.sharespace.user.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileScreenViewModel(
    private val userSessionRepository: UserSessionRepository,
    private val profileRepository: ProfileRepository,

    ) : ViewModel() {
    // backing state
    private val _user = mutableStateOf<User?>(null)
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    private val _invites = MutableStateFlow<List<Room>>(emptyList())

    // public streams
    val user: MutableState<User?> = _user
    val rooms: StateFlow<List<Room>> = _rooms
    val invites: MutableStateFlow<List<Room>> = _invites

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
    }

    fun acceptInvite() {

    }

    fun declineInvite() {

    }


    fun loadData() {
//        println("Loading data with token: $token")
//        println("hfudsihujksalhfshfjlsdhk")
        viewModelScope.launch {
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    return@launch
                }
                val apiUser = profileRepository.getUser(token)
                _user.value = User(
                    id = apiUser.id,
                    name = apiUser.name,
                    username = apiUser.username,
                    photoUrl = apiUser.profilePictureUrl
                )

                val (joinedRooms, roomInvites) = profileRepository.getRoomsAndInvites(token)
//                println("Got ${joinedRooms.size} joined rooms")
//                println("Got ${roomInvites.size} invites")


                _rooms.value = joinedRooms.map { room ->
                    Room(
                        id = room.id.toString(),
                        name = room.name,
                        members = room.members.size,
                        due = room.balanceDue,
                        notifications = room.alerts,
                        photoUrl = room.pictureUrl
                    )
                }

                _invites.value = roomInvites.map { invite ->
                    Room(
                        id = invite.roomId.toString(),
                        name = "Room ${invite.roomId}",
                        members = 0, // if you need details, another API call is needed
                        due = 0f,
                        notifications = 0,
                        photoUrl = null
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println("Error loading profile data: ${e.message}")
            }
        }
    }
}
