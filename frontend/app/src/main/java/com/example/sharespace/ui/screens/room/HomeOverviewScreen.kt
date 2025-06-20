package com.example.sharespace.ui.screens.room

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeOverviewScreen(
    onUserProfileClick: () -> Unit,
    onCreateRoomClick: () -> Unit,
    onRoomClick: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                text = "Home overview Screen"
            )
            Button(
                onClick = onUserProfileClick,
                modifier = Modifier.widthIn(min = 250.dp)
            ) {
                Text(text = "Go to user profile")
            }
            Button(
                onClick = onRoomClick,
                modifier = Modifier.widthIn(min = 250.dp)
            ) {
                Text(text = "Go to room")
            }
            Button(
                onClick = onCreateRoomClick,
                modifier = Modifier.widthIn(min = 250.dp)
            ) {
                Text(text = "Go to create room")
            }

        }
    }

}