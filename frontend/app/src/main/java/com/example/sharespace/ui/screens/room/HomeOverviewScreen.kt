package com.example.sharespace.ui.screens.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.ui.components.NavigationHeader
// screens/room/HomeOverviewScreen.kt
@Composable
fun HomeOverviewScreen(
    onUserProfileClick: () -> Unit = {},
    onCreateRoomClick: () -> Unit = {},
    onRoomClick: () -> Unit = {}
) {
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
        }
    }
}

