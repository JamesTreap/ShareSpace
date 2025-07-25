@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sharespace.room.ui.roomSummary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.core.ui.components.Avatar
import com.example.sharespace.room.ui.roomSummary.components.RecentBillsSection
import com.example.sharespace.room.ui.roomSummary.components.RoomSummaryTopAppBar
import com.example.sharespace.room.ui.roomSummary.components.RoommatesSection
import com.example.sharespace.room.ui.roomSummary.components.SectionHeader
import com.example.sharespace.room.viewmodel.RoomDetailsUiState
import com.example.sharespace.room.viewmodel.RoomSummaryRoommatesUiState
import com.example.sharespace.room.viewmodel.RoomSummaryViewModel
import java.time.format.DateTimeFormatter

@Composable
fun RoomSummaryScreen(
    viewModel: RoomSummaryViewModel = viewModel(factory = RoomSummaryViewModel.Companion.Factory),
    onViewBillsClick: () -> Unit,
    onAddRoommateClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onViewTasksClick: () -> Unit,
    onFinanceManagerClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val bills by viewModel.bills.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val roomDetailsState = viewModel.roomDetailsUiState
    val roommatesState = viewModel.roommatesUiState

    Scaffold(
        topBar = {
            val topBarTitle: String
            val topBarSubtitle: String
            var showRetryDetailsButton = false

            when (roomDetailsState) {
                is RoomDetailsUiState.Success -> {
                    topBarTitle = roomDetailsState.roomDetails.name
                    topBarSubtitle = roomDetailsState.roomDetails.pictureUrl ?: "Details available"
                }

                is RoomDetailsUiState.Loading -> {
                    topBarTitle = "Loading Room..."
                    topBarSubtitle = "Please wait"
                }

                is RoomDetailsUiState.Error -> {
                    topBarTitle = "Error"
                    topBarSubtitle = "Could not load room details"
                    showRetryDetailsButton = true
                }
            }
            RoomSummaryTopAppBar(
                address = topBarTitle,
                subtitle = topBarSubtitle,
                onNavigateBack = onNavigateBack,
                showRetry = showRetryDetailsButton,
                onRetry = { viewModel.fetchRoomDetails() }
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Button(
                onClick = onFinanceManagerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = "Finance Manager")
            }
            RecentBillsSection(
                bills = bills,
                onPay = viewModel::payBill,
                onViewAll = onViewBillsClick
            )
            when (roommatesState) {
                is RoomSummaryRoommatesUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp), // Keep padding for visual separation
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is RoomSummaryRoommatesUiState.Success -> {
                    RoommatesSection(
                        roommates = roommatesState.roommates,
                        onAdd = onAddRoommateClick,
                        onViewAll = { /* TODO for roommates view all */ }
                    )
                }

                is RoomSummaryRoommatesUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp), // Keep padding
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Failed to load roommates.",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchRoomMembers() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            UpcomingTasksSection(
                tasks = tasks,
                onToggleDone = viewModel::toggleTaskDone,
                onAdd = onAddTaskClick,
                onViewAll = onViewTasksClick,
            )
        }
    }
}


@Composable
fun UpcomingTasksSection(
    tasks: List<Task>,
    onAdd: () -> Unit,
    onToggleDone: (Task) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Upcoming Tasks", actionText = "+ Add Task", onAction = onAdd
        )
        if (tasks.isEmpty()) {
            Text(
                "No upcoming tasks.",
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Button( // "View all" button consistent with your structure
                onClick = onViewAll, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Text("View all / Add Task")
            }
            Spacer(Modifier.height(8.dp))
            return@Column
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            items(tasks) { task ->
                ListItem(headlineContent = { Text(task.title) }, supportingContent = {
                    Text(
                        task.dueDate.format(DateTimeFormatter.ofPattern("MMM d | h:mm a"))
                            ?: "No due date"
                    )
                }, leadingContent = {
                    Avatar(
                        photoUrl = null,
                        size = 40.dp,
                        contentDescription = "Task assignment placeholder"
                    )
                }, trailingContent = {
                    IconButton(onClick = { onToggleDone(task) }) {
                        Icon(
                            imageVector = if (task.isDone) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = if (task.isDone) "Mark as not done" else "Mark as done"
                        )
                    }
                })
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp))
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onViewAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Text("View all tasks")
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}