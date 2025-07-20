package com.example.sharespace.ui.screens.auth

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharespace.R
import com.example.sharespace.core.data.local.TokenStorage
import com.example.sharespace.core.data.remote.ApiClient
import com.example.sharespace.core.data.remote.LoginRequest
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            StyledTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val response = ApiClient.apiService.login(
                                LoginRequest(
                                    username,
                                    password
                                )
                            )
                            if (response.isSuccessful) {
                                val token = response.body()?.token
                                if (token != null) {
                                    TokenStorage.saveToken(context, token)
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Invalid token received"
                                }
                            } else {
                                errorMessage = "Login failed: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log In")
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



            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }

}
