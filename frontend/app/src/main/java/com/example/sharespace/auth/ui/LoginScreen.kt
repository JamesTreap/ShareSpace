package com.example.sharespace.ui.screens.auth

// No need to import Application here if the factory isn't passed directly
// LocalContext might still be needed if you were to access application directly for other reasons
// but not for the factory itself if it's a singleton in ViewModel
// ViewModelProvider is not needed if you directly use the singleton factory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.core.ui.components.StyledTextField

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
    val uiState = loginViewModel.loginUiState

    // Handle navigation on LoginSuccess
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.LoginSuccess) {
            onLoginSuccess()
            loginViewModel.onLoginHandled() // Reset state after navigation
        }
    }

    // Extract data from Stable state
    val currentUsername = (uiState as? LoginUiState.Stable)?.usernameInput ?: ""
    val currentPassword = (uiState as? LoginUiState.Stable)?.passwordInput ?: ""
    val errorMessage = (uiState as? LoginUiState.Stable)?.errorMessage
    val isLoggingIn = (uiState as? LoginUiState.Stable)?.isLoggingIn ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        StyledTextField(
            value = currentUsername,
            onValueChange = { loginViewModel.onUsernameChange(it) },
            label = { Text("Username") },
            isError = errorMessage != null,
            enabled = !isLoggingIn,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        StyledTextField(
            value = currentPassword,
            onValueChange = { loginViewModel.onPasswordChange(it) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = errorMessage != null,
            enabled = !isLoggingIn,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { loginViewModel.loginUser() },
            enabled = !isLoggingIn && uiState is LoginUiState.Stable,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoggingIn) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Log In")
            }
        }

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }
    }
}

