package com.example.sharespace.room.ui.roomSummary.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomSummaryTopAppBar(
    address: String,
    subtitle: String,
    onNavigateBack: () -> Unit,
    showRetry: Boolean = false,
    onRetry: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    TopAppBar(title = {
        Column {
            Text(text = address, style = MaterialTheme.typography.titleLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }, navigationIcon = {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    }, actions = {
        TextButton(onClick = onEditClick) { // Or IconButton as preferred
            Text("Edit")
        }
        if (showRetry) {
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    })
}