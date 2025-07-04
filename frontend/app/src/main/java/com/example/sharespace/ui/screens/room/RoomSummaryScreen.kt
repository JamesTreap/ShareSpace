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
import com.example.sharespace.ui.components.NavigationHeader
import com.example.sharespace.ui.screens.finance.FinanceManagerScreen

@Composable
fun RoomSummaryScreen(
    onNavigateBack: (() -> Unit)? = null,
    onViewBillsClick: () -> Unit,
    onAddRoommateClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onViewTasksClick: () -> Unit,
    onFinanceManagerClick: () -> Unit
) {
    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Room Summary",
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
                text = "Room summary screen"
            )
            Button(
                onClick = onViewBillsClick,
                modifier = Modifier.widthIn(min = 250.dp)
            ) {
                Text(text = "View all bills")
            }
            Button(
                onClick = onAddRoommateClick,
                modifier = Modifier.widthIn(min = 250.dp)
            ) {
                Text(text = "Add roommate")
            }
            Button(
                onClick = onAddTaskClick,
                modifier = Modifier.widthIn(min = 250.dp)
            ) {
                Text(text = "Add tasks")
            }
            Button(
                onClick = onViewTasksClick,
                modifier = Modifier.widthIn(min = 250.dp)
            ) {
                Text(text = "View all tasks")
            }
            Button(
                onClick = onFinanceManagerClick,
                modifier = Modifier.widthIn(min = 250.dp)
            ) {
                Text(text = "Finance Manager")
            }
        }
    }
}