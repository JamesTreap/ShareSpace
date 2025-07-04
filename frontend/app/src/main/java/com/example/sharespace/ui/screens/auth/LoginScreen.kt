package com.example.sharespace.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.sharespace.ui.components.NavigationHeader

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {}
) {
    // No back button for login screen
    Column {
        Text("Login Screen")
        Button(onClick = onLoginSuccess) {
            Text("Login")
        }
    }
}