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
sealed interface SignupUiState {
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
        val nameInput: String = "",
        val errorMessage: String? = null,
        val isLoggingIn: Boolean = false,
        val profilePicture: Int = 0,
    ) : SignupUiState

    /**
     * Represents a successful login, typically used to trigger navigation.
     */
    object SignupSuccess : SignupUiState
}

class SignupViewModel(
    private val authRepository: AuthRepository,
    private val userSessionRepository: UserSessionRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    /**
     * The current UI state for the Login screen.
     */
    var signupUiState: SignupUiState by mutableStateOf(SignupUiState.Stable())
        private set

    fun onUsernameChange(username: String) {
        val currentStableState = signupUiState as? SignupUiState.Stable ?: SignupUiState.Stable()
        signupUiState = currentStableState.copy(usernameInput = username, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        val currentStableState = signupUiState as? SignupUiState.Stable ?: SignupUiState.Stable()
        signupUiState = currentStableState.copy(passwordInput = password, errorMessage = null)
    }

    fun onNameChange(name: String) {
        val currentStableState = signupUiState as? SignupUiState.Stable ?: SignupUiState.Stable()
        signupUiState = currentStableState.copy(nameInput = name, errorMessage = null)
    }

    fun onIconSelected(iconIndex: Int) {
        val currentStableState = signupUiState as? SignupUiState.Stable ?: SignupUiState.Stable()
        signupUiState = currentStableState.copy(profilePicture = iconIndex, errorMessage = null)
    }

    fun signupUser() {
        val currentInputState = signupUiState as? SignupUiState.Stable
        if (currentInputState == null) {
            // Should not happen if UI is driven by Stable state, but good for safety
            signupUiState = SignupUiState.Stable(errorMessage = "An unexpected UI state occurred.")
            return
        }

        val name = currentInputState.nameInput
        val username = currentInputState.usernameInput
        val password = currentInputState.passwordInput


        if (username.isBlank() || password.isBlank() || name.isBlank()) {
            signupUiState =
                currentInputState.copy(errorMessage = "Name, username, and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            signupUiState = currentInputState.copy(isLoggingIn = true, errorMessage = null)
            try {
                // Ensure authRepository is available and used here
                // I passed in the username for the email field so that
                // the unique requirement is met for the email field
                val response = authRepository.createAccount(username, password, username)
                val token =
                    response.token
                if (token != null) {
                    userSessionRepository.saveUserToken(token)
                    val apiUser = profileRepository.getUser(token)
                    userSessionRepository.saveCurrentUserId(apiUser.id)
                    signupUiState = SignupUiState.SignupSuccess

                    // Modify the user profile with the provided name and icon
                    val pictureUrl = "pfp${currentInputState.profilePicture}"
                    profileRepository.patchProfile(
                        token = token,
                        name = name,
                        username = username,
                        profilePictureUrl = pictureUrl
                    )
                } else {
                    signupUiState = currentInputState.copy(
                        isLoggingIn = false,
                        errorMessage = "Invalid token received"
                    )
                }
            } catch (e: HttpException) {
                signupUiState = currentInputState.copy(
                    isLoggingIn = false,
                    errorMessage = "Signup failed: ${e.code()} - ${e.message()}"
                )
            } catch (e: IOException) {
                signupUiState = currentInputState.copy(
                    isLoggingIn = false,
                    errorMessage = "Network error: ${e.localizedMessage}"
                )
            } catch (e: IllegalStateException) { // For specific errors like null response body from repository
                signupUiState = currentInputState.copy(
                    isLoggingIn = false,
                    errorMessage = "Error: ${e.localizedMessage}"
                )
            } catch (e: Exception) { // Generic catch-all for unexpected errors
                signupUiState = currentInputState.copy(
                    isLoggingIn = false,
                    errorMessage = "An unexpected error occurred: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Call this after navigation has been handled by the UI to reset the state.
     */
    fun onSignupHandled() {
        // Reset to Stable state. You can choose to clear inputs or preserve them.
        // Clearing them is often standard after a successful action and navigation.
        signupUiState = SignupUiState.Stable()
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

                SignupViewModel(
                    authRepository = authRepository,
                    userSessionRepository = userSessionRepository,
                    profileRepository = profileRepository
                )
            }
        }
    }
}
