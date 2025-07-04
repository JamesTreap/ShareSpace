package com.example.sharespace.ui.screens.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sharespace.ui.components.NavigationHeader

@Composable
fun AddTaskScreen(onNavigateBack: (() -> Unit)? = null) {
    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Add Task",
                onNavigateBack = onNavigateBack
            )
        },
        modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                text = "Add task screen"
            )
        }
    }
}