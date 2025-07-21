package com.example.sharespace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sharespace.ShareSpaceScreens
import com.example.sharespace.core.data.repository.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


// Represents the state for the initial destination
sealed class InitialDestinationState {
    data object Loading : InitialDestinationState()
    data class Loaded(val startDestinationRoute: String) : InitialDestinationState()
}

class MainViewModel(
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _initialDestinationState =
        MutableStateFlow<InitialDestinationState>(InitialDestinationState.Loading)
    val initialDestinationState = _initialDestinationState.asStateFlow()

    init {
        determineInitialDestination()
    }

    private fun determineInitialDestination() {
        viewModelScope.launch {
            val token = userSessionRepository.userTokenFlow.first() // Get the first emitted value

            val destinationRoute = if (!token.isNullOrEmpty()) {
                ShareSpaceScreens.MainProfile.name
            } else {
                ShareSpaceScreens.Login.name
            }
            _initialDestinationState.value = InitialDestinationState.Loaded(destinationRoute)
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
                val userSessionRepository = application.container.userSessionRepository
                MainViewModel(userSessionRepository = userSessionRepository)
            }
        }
    }
}
