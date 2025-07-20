package com.example.sharespace.ui.screens.auth

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharespace.R
import com.example.sharespace.core.data.local.TokenStorage
import com.example.sharespace.core.data.remote.ApiClient
//import com.example.sharespace.core.data.remote.LoginRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.core.ui.components.StyledTextField
import kotlinx.coroutines.launch
import androidx.core.graphics.scale
import com.example.sharespace.core.ui.theme.AquaAccent
import com.example.sharespace.core.ui.theme.TextPrimary
import com.example.sharespace.core.ui.theme.TextSecondary

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

    val patternImage = ImageBitmap.imageResource(id = R.drawable.auth_background)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background: tile the pattern image
        val context = LocalContext.current
        val scaledBitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.auth_background)
                .scale((patternImage.width * 2f).toInt(), (patternImage.height * 2f).toInt())
        val scaledImageBitmap = scaledBitmap.asImageBitmap()
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tileWidth = scaledImageBitmap.width
            val tileHeight = scaledImageBitmap.height

            for (x in 0 until size.width.toInt() step tileWidth) {
                for (y in 0 until size.height.toInt() step tileHeight) {
                    drawImage(
                        image = scaledImageBitmap,
                        topLeft = Offset(x.toFloat(), y.toFloat()),
                        alpha = 0.3f
                    )
                }
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ShareSpace",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = AquaAccent,
                    fontSize = 48.sp
                ))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Sign in to your account",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary
                ))
            Text("Welcome back! Fill out the fields below",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                ))

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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("New user here? ")
                Text(
                    text = "Sign up",
                    color = AquaAccent,
                    modifier = Modifier.clickable {
                        // Handle sign-up click action here
                        println("Sign up clicked")
                    }
                )
            }

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }
    }
}
}

