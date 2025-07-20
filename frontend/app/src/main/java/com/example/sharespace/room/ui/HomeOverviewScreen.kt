package com.example.sharespace.ui.screens.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.core.ui.components.NavigationHeader
import com.example.sharespace.room.viewmodel.HomeOverviewViewModel
import com.example.sharespace.room.viewmodel.NavigationEvent // Import the NavigationEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeOverviewScreen(
    viewModel: HomeOverviewViewModel = viewModel(factory = HomeOverviewViewModel.Factory),
    onUserProfileClick: () -> Unit = {},
    onCreateRoomClick: () -> Unit = {},
    onRoomClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}, // This is the original "Log In" button
    onLogoutNavigation: () -> Unit = {} // Callback for navigation after logout
) {
    var isLoggingOutState by remember { mutableStateOf(false) }

    // Collect navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateToLogin -> {
                    onLogoutNavigation() // Trigger the navigation
                    // isLoggingOutState will reset if the screen is left and recomposed,
                    // or if this Composable is removed from the composition.
                }
            }
        }
    }

    // Collect logging out status
    LaunchedEffect(Unit) {
        viewModel.isLoggingOut.collectLatest { isProcessing ->
            isLoggingOutState = isProcessing
        }
    }

    Scaffold(
        topBar = {
            NavigationHeader(
                title = "ShareSpace",
                actions = {
                    IconButton(onClick = onUserProfileClick) {
                        Icon(Icons.Default.Person, "Profile")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Home Overview", style = MaterialTheme.typography.headlineMedium)

            Button(
                onClick = onCreateRoomClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Room")
            }

            Button(
                onClick = onRoomClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View My Room")
            }

            Button(
                onClick = onLoginClick, // Original "Log In" button
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log In")
            }

            Button(
                onClick = {
                    if (!isLoggingOutState) { // Prevent multiple clicks
                        viewModel.logoutUser()
                    }
                },
                enabled = !isLoggingOutState, // Disable button while logging out
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoggingOutState) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Log Out")
                }
            }
        }
    }
}

