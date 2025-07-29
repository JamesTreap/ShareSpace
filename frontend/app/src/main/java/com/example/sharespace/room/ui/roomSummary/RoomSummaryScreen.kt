@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sharespace.room.ui.roomSummary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.room.ui.roomSummary.components.CalendarSection
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
    onFinanceManagerClick: () -> Unit, // Assuming this is for a future feature
    onNavigateBack: () -> Unit
) {
    // val bills by viewModel.bills.collectAsState() // Keep if used

    val roomDetailsState: RoomDetailsUiState = viewModel.roomDetailsUiState
    val roommatesUiState: RoomSummaryRoommatesUiState = viewModel.roommatesUiState
    val tasksUiState: TasksUiState = viewModel.tasksUiState

    // Correctly collect StateFlow
    val currentUserId: String? by viewModel.currentUserIdString.collectAsState()

    val calendarUiState by viewModel.calendarUiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedRoommates by viewModel.selectedRoommates.collectAsState()

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
                .padding(vertical = 16.dp)
                .fillMaxSize()
        ) {
            // Add Finance Manager Button here
            Button(
                onClick = onFinanceManagerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Finance Manager",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Finance Manager",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            RoommatesSection(
                roommatesUiState = roommatesUiState,
                onAdd = onAddRoommateClick,
                onRetry = viewModel::fetchRoomMembers,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(16.dp))

            UpcomingTasksSection(
                tasksUiState = tasksUiState,
                roommatesUiState = roommatesUiState,
                currentUserId = currentUserId,
                onAdd = onAddTaskClick,
                onToggleDone = { task, newStatus ->
                    viewModel.updateTaskStatus(task, newStatus)
                },
                onViewAll = onViewTasksClick,
                onRetry = viewModel::fetchTasks,
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(16.dp))

//            CalendarSection(
//                calendarUiState = calendarUiState,
//                selectedDate = selectedDate,
//                onDateSelected = viewModel::updateSelectedDate,
//                roommates = if (roommatesUiState is RoomSummaryRoommatesUiState.Success)
//                    roommatesUiState.roommates else emptyList(),
//                selectedRoommates = selectedRoommates,
//                onRoommateSelectionChanged = viewModel::updateSelectedRoommates,
//                currentUserId = currentUserId,
//                onRetry = viewModel::fetchCalendarData,
//                modifier = Modifier.padding(horizontal = 12.dp)
//            )
        }
    }
}