package com.example.sharespace.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sharespace.ui.components.NavigationHeader

@Composable
fun EditProfileScreen(
    onNavigateBack: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Edit Profile",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text("Edit Profile Screen - Content Coming Soon")
        }
    }
}