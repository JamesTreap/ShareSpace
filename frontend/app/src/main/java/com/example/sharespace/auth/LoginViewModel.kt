package com.example.sharespace.ui.screens.auth

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
import com.example.sharespace.core.data.repository.UserSessionRepository
import com.example.sharespace.data.repository.AuthRepository
import com.example.sharespace.user.data.repository.ProfileRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Represents the different states for the Login Screen UI.
 */
sealed interface LoginUiState {
    /**
     * Represents the stable state, ready for input or displaying previous errors.
     * @param usernameInput Current username input by the user.
     * @param passwordInput Current password input by the user.
     * @param errorMessage Optional error message to display.
     * @param isLoggingIn True if a login attempt is currently in progress, false otherwise.
     */
    data class Stable(
        val usernameInput: String = "",
        val passwordInput: String = "",
        val errorMessage: String? = null,
        val isLoggingIn: Boolean = false
    ) : LoginUiState

    /**
     * Represents a successful login, typically used to trigger navigation.
     */
    object LoginSuccess : LoginUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userSessionRepository: UserSessionRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    /**
     * The current UI state for the Login screen.
     */
    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Stable())
        private set

    fun onUsernameChange(username: String) {
        val currentStableState = loginUiState as? LoginUiState.Stable ?: LoginUiState.Stable()
        loginUiState = currentStableState.copy(usernameInput = username, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        val currentStableState = loginUiState as? LoginUiState.Stable ?: LoginUiState.Stable()
        loginUiState = currentStableState.copy(passwordInput = password, errorMessage = null)
    }

    fun loginUser() {
        val currentInputState = loginUiState as? LoginUiState.Stable
        if (currentInputState == null) {
            // Should not happen if UI is driven by Stable state, but good for safety
            loginUiState = LoginUiState.Stable(errorMessage = "An unexpected UI state occurred.")
            return
        }

        val username = currentInputState.usernameInput
        val password = currentInputState.passwordInput

        if (username.isBlank() || password.isBlank()) {
            loginUiState =
                currentInputState.copy(errorMessage = "Username and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            loginUiState = currentInputState.copy(isLoggingIn = true, errorMessage = null)
            try {
                // Ensure authRepository is available and used here
                val response = authRepository.login(username, password)
                val token =
                    response.token // Assuming your DTO from authRepository.login() has a 'token' field
                if (token != null) {
                    userSessionRepository.saveUserToken(token)
                    val apiUser = profileRepository.getUser(token)
                    userSessionRepository.saveCurrentUserId(apiUser.id)
                    loginUiState = LoginUiState.LoginSuccess
                } else {
                    loginUiState = currentInputState.copy(
                        isLoggingIn = false,
                        errorMessage = "Invalid token received"
                    )
                }
            } catch (e: HttpException) {
                loginUiState = currentInputState.copy(
                    isLoggingIn = false,
                    errorMessage = "Login failed: ${e.code()} - ${e.message()}"
                )
            } catch (e: IOException) {
                loginUiState = currentInputState.copy(
                    isLoggingIn = false,
                    errorMessage = "Network error: ${e.localizedMessage}"
                )
            } catch (e: IllegalStateException) { // For specific errors like null response body from repository
                loginUiState = currentInputState.copy(
                    isLoggingIn = false,
                    errorMessage = "Error: ${e.localizedMessage}"
                )
            } catch (e: Exception) { // Generic catch-all for unexpected errors
                loginUiState = currentInputState.copy(
                    isLoggingIn = false,
                    errorMessage = "An unexpected error occurred: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Call this after navigation has been handled by the UI to reset the state.
     */
    fun onLoginHandled() {
        // Reset to Stable state. You can choose to clear inputs or preserve them.
        // Clearing them is often standard after a successful action and navigation.
        loginUiState = LoginUiState.Stable()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // Get the Application instance
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)

                // Retrieve dependencies from the application's DI container
                val userSessionRepository = application.container.userSessionRepository
                val authRepository = application.container.authRepository
                val profileRepository = application.container.profileRepository

                LoginViewModel(
                    authRepository = authRepository,
                    userSessionRepository = userSessionRepository,
                    profileRepository = profileRepository
                )
            }
        }
    }
}
