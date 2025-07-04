package com.example.sharespace.ui.screens.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@Composable
fun TasksListScreen(
    onNavigateBack: (() -> Unit)? = null,
    onAddTaskClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Tasks",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onAddTaskClick) {
                        Icon(Icons.Default.Add, "Add Task")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Tasks List", style = MaterialTheme.typography.headlineMedium)
            Text("Tasks content coming soon...")
        }
    }
}