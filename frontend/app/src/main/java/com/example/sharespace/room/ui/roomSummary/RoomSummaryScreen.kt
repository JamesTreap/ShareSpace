@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sharespace.room.ui.roomSummary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    viewModel: RoomSummaryViewModel = viewModel(factory = RoomSummaryViewModel.Factory),
    onViewBillsClick: () -> Unit, // Keep if RecentBillsSection is re-added
    onAddRoommateClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onViewTasksClick: () -> Unit,
    // onFinanceManagerClick: () -> Unit, // Assuming this is for a future feature
    onNavigateBack: () -> Unit
) {
    // val bills by viewModel.bills.collectAsState() // Keep if used

    val roomDetailsState: RoomDetailsUiState = viewModel.roomDetailsUiState
    val roommatesUiState: RoomSummaryRoommatesUiState = viewModel.roommatesUiState
    val tasksUiState: TasksUiState = viewModel.tasksUiState

    // Correctly collect StateFlow
    val currentUserId: String? by viewModel.currentUserIdString.collectAsState()

    Scaffold(
        topBar = {
            val topBarTitle: String
            val topBarSubtitle: String
            var showRetryDetailsButton = false

            // Use the directly accessed state
            when (val currentRoomDetails = roomDetailsState) {
                is RoomDetailsUiState.Success -> {
                    topBarTitle = currentRoomDetails.roomDetails.name
                    topBarSubtitle =
                        currentRoomDetails.roomDetails.pictureUrl ?: "Details available"
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
                onRetry = viewModel::fetchRoomDetails
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            RoommatesSection(
                roommatesUiState = roommatesUiState, // Pass the UiState directly
                onAdd = onAddRoommateClick,
                onViewAll = { /* TODO: Navigate to all roommates screen */ },
                onRetry = viewModel::fetchRoomMembers
            )

            Spacer(Modifier.height(16.dp))

            UpcomingTasksSection(
                tasksUiState = tasksUiState, // Pass the UiState directly
                currentUserId = currentUserId,
                onAdd = onAddTaskClick,
                onToggleDone = { task, newStatus ->
                    viewModel.updateTaskStatus(task, newStatus)
                },
                onViewAll = onViewTasksClick,
                onRetry = viewModel::fetchTasks
            )
        }
    }
}