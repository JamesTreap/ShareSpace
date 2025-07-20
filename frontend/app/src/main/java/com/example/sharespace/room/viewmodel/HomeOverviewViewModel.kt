package com.example.sharespace.room.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sharespace.ShareSpaceApplication
import com.example.sharespace.core.data.repository.UserSessionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class HomeOverviewViewModel(
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    // For side effects like navigation after logout
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    // No complex UI state is needed if the screen is static except for logout.
    // However, we might want a simple state to disable the logout button during the operation.
    private val _isLoggingOut = MutableSharedFlow<Boolean>() // Simple signal for button state
    val isLoggingOut = _isLoggingOut.asSharedFlow()


    fun logoutUser() {
        viewModelScope.launch {
            _isLoggingOut.emit(true) // Signal that logout process has started
            userSessionRepository.clearAllSessionData() // Clear token and any other session data
            _navigationEvent.emit(NavigationEvent.NavigateToLogin)
            // No need to emit _isLoggingOut.emit(false) here, as navigation will occur.
            // The ViewModel instance might be cleared if the screen is removed from backstack.
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)
                val userSessionRepository = application.container.userSessionRepository
                HomeOverviewViewModel(
                    userSessionRepository = userSessionRepository
                )
            }
        }
    }
}

// Keep NavigationEvent sealed interface (can be in a separate file or within ViewModel file)
sealed interface NavigationEvent {
    object NavigateToLogin : NavigationEvent
}

