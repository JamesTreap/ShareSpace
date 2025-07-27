package com.example.sharespace.auth.ui

import ScreenHeader
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.R
import com.example.sharespace.core.ui.components.StyledTextField
import com.example.sharespace.core.ui.theme.AquaAccent
import com.example.sharespace.core.ui.theme.TextPrimary
import com.example.sharespace.core.ui.theme.TextSecondary
import com.example.sharespace.ui.screens.auth.SignupUiState
import com.example.sharespace.ui.screens.auth.SignupViewModel

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val signupViewModel: SignupViewModel = viewModel(factory = SignupViewModel.Factory)
    val uiState = signupViewModel.signupUiState

    // Handle navigation on LoginSuccess
    LaunchedEffect(uiState) {
        if (uiState is SignupUiState.SignupSuccess) {
            onSignupSuccess()
            signupViewModel.onSignupHandled() // Reset state after navigation
        }
    }

    // Extract data from Stable state
    val currentUsername = (uiState as? SignupUiState.Stable)?.usernameInput ?: ""
    val currentPassword = (uiState as? SignupUiState.Stable)?.passwordInput ?: ""
    val currentName = (uiState as? SignupUiState.Stable)?.nameInput ?: ""
    val errorMessage = (uiState as? SignupUiState.Stable)?.errorMessage
    val isLoggingIn = (uiState as? SignupUiState.Stable)?.isLoggingIn ?: false
    val selectedIconIndex = (uiState as? SignupUiState.Stable)?.profilePicture ?: 0

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

        ScreenHeader(
            title = "", onNavigateBack, actions = {}
        )
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
            Text("Create New Account",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary
                ))
            Text("Welcome! Fill out the fields below",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                ))

            Spacer(modifier = Modifier.height(24.dp))
            StyledTextField(
                value = currentName,
                onValueChange = { signupViewModel.onNameChange(it) },
                label = { Text("Name") },
                isError = errorMessage != null,
                enabled = !isLoggingIn,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            StyledTextField(
                value = currentUsername,
                onValueChange = { signupViewModel.onUsernameChange(it) },
                label = { Text("Username") },
                isError = errorMessage != null,
                enabled = !isLoggingIn,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            StyledTextField(
                value = currentPassword,
                onValueChange = { signupViewModel.onPasswordChange(it) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorMessage != null,
                enabled = !isLoggingIn,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Select a Profile Icon",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.height(160.dp)
            ) {
                items(10) { index ->
                    val resId = remember {
                        context.resources.getIdentifier(
                            "pfp$index", "drawable", context.packageName
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 4.dp,
                                color = if (index == selectedIconIndex) AquaAccent else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { signupViewModel.onIconSelected(index) }) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "Profile Picture $index",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { signupViewModel.signupUser() },
                enabled = !isLoggingIn && uiState is SignupUiState.Stable,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoggingIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sign Up")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))



            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

