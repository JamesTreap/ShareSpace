package com.example.sharespace.user.viewmodel

import androidx.compose.material3.SnackbarHostState
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

class EditProfileScreenViewModel(
    private val userSessionRepository: UserSessionRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _user = mutableStateOf<User?>(null)

    // public streams
    val user: MutableState<User?> = _user

    val name = mutableStateOf("")
    val username = mutableStateOf("")
    val selectedIconIndex = mutableStateOf(0)

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

                name.value = apiUser.name
                username.value = apiUser.username
                selectedIconIndex.value = extractIndexFromUrl(apiUser.profilePictureUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error loading profile data: ${e.message}")
            }
        }
    }

    fun onNameChange(newName: String) {
        name.value = newName
    }

    fun onUsernameChange(newUsername: String) {
        username.value = newUsername
    }

    fun onIconSelected(index: Int) {
        selectedIconIndex.value = index
    }

    fun updateProfile(
        onNavigateBack: () -> Unit,
        snackbarHostState: SnackbarHostState
    ) {
        viewModelScope.launch {
            try {
                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    return@launch
                }
                val pictureUrl = "pfp${selectedIconIndex.value}"
                profileRepository.patchProfile(
                    token = token,
                    name = name.value,
                    username = username.value,
                    profilePictureUrl = pictureUrl
                )

                println("Profile updated successfully")
                onNavigateBack()
            } catch (e: Exception) {
//                e.printStackTrace()
                snackbarHostState.showSnackbar("Error: Username already taken")

            }
        }
    }

    private fun extractIndexFromUrl(url: String?): Int {
        return url?.takeIf { it.contains("pfp") }
            ?.substringAfterLast("pfp")
            ?.substringBefore('.')
            ?.toIntOrNull() ?: 0
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)
                val userSessionRepository = application.container.userSessionRepository
                val profileRepository = application.container.profileRepository
                EditProfileScreenViewModel(
                    userSessionRepository = userSessionRepository,
                    profileRepository = profileRepository
                )
            }
        }
    }

}
