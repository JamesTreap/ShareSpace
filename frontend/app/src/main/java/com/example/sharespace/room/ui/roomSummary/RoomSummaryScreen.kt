@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sharespace.room.ui.roomSummary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.sharespace.room.ui.roomSummary.components.RoomSummaryTopAppBar
import com.example.sharespace.room.ui.roomSummary.components.RoommatesSection
import com.example.sharespace.room.ui.roomSummary.components.UpcomingTasksSection
import com.example.sharespace.room.viewmodel.RoomDetailsUiState
import com.example.sharespace.room.viewmodel.RoomSummaryRoommatesUiState
import com.example.sharespace.room.viewmodel.RoomSummaryViewModel
import com.example.sharespace.room.viewmodel.TasksUiState

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
    val tasksState = viewModel.tasksUiState
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
                onRetry = { viewModel.fetchRoomDetails() })
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
//            RecentBillsSection(
//                bills = bills, onPay = viewModel::payBill, onViewAll = onViewBillsClick
//            )
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
                        onViewAll = { /* TODO for roommates view all */ })
                }

                is RoomSummaryRoommatesUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp), // Keep padding
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Failed to load roommates.", color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchRoomMembers() }) {
                            Text("Retry")
                        }
                    }
                }
            }


            // render tasks section
            when (val currentTasksState = tasksState) { // Use the collected state
                is TasksUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is TasksUiState.Success -> {
                    UpcomingTasksSection(
                        tasks = currentTasksState.tasks,
                        onToggleDone = { },
                        onAdd = onAddTaskClick,
                        onViewAll = onViewTasksClick,
                    )
                }

                is TasksUiState.Empty -> {
                    // You might want a specific UI for when there are no tasks
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No upcoming tasks.")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onAddTaskClick) { // Allow adding a task
                            Text("Add Task")
                        }
                    }
                }

                is TasksUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Failed to load tasks.", color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchTasks() }) { // Retry fetching tasks
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}