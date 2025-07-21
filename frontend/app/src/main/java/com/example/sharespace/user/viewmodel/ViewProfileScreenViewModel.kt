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
import com.example.sharespace.user.data.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ViewProfileScreenViewModel(
    private val userSessionRepository: UserSessionRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    // backing state
    private val _user = mutableStateOf<User?>(null)

    // public streams
    val user: MutableState<User?> = _user


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)
                val userSessionRepository = application.container.userSessionRepository
                val profileRepository = application.container.profileRepository
                ViewProfileScreenViewModel(
                    userSessionRepository = userSessionRepository,
                    profileRepository = profileRepository
                )
            }
        }
    }

    fun loadData() {
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

            } catch (e: Exception) {
                e.printStackTrace()
                println("Error loading profile data: ${e.message}")
            }
        }
    }
}