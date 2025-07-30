@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sharespace.room.ui.roomSummary

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.room.ui.roomSummary.components.RecentBillsSection
import com.example.sharespace.room.ui.roomSummary.components.RoomSummaryTopAppBar
import com.example.sharespace.room.ui.roomSummary.components.RoommatesSection
import com.example.sharespace.room.ui.roomSummary.components.UpcomingTasksSection
import com.example.sharespace.room.viewmodel.BillsUiState
import com.example.sharespace.room.viewmodel.RoomDetailsUiState
import com.example.sharespace.room.viewmodel.RoomSummaryRoommatesUiState
import com.example.sharespace.room.viewmodel.RoomSummaryViewModel
import com.example.sharespace.room.viewmodel.TasksUiState

@Composable
fun RoomSummaryScreen(
    viewModel: RoomSummaryViewModel = viewModel(factory = RoomSummaryViewModel.Factory),
    onAddRoommateClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onViewTasksClick: () -> Unit,
    onFinanceManagerClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onAddBillClick: () -> Unit,
    onEditClick: () -> Unit,
    onCalendarClick: () -> Unit = {},
) {
    val roomDetailsState: RoomDetailsUiState = viewModel.roomDetailsUiState
    val roommatesUiState: RoomSummaryRoommatesUiState = viewModel.roommatesUiState
    val tasksUiState: TasksUiState = viewModel.tasksUiState
    val billsUiState: BillsUiState = viewModel.billsUiState

    val currentUserIdString: String? by viewModel.currentUserIdString.collectAsState()
    val currentUserIdInt: Int? by viewModel.currentUserIdInt.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            Log.d("RoomSummaryScreen", "Lifecycle RESUMED, refreshing data.")
            viewModel.refreshAllData()
        }
    }

    Scaffold(
        topBar = {
            val topBarTitle: String
            val topBarSubtitle: String
            var showRetryDetailsButton = false

            // Use the directly accessed state
            when (val currentRoomDetails = roomDetailsState) {
                is RoomDetailsUiState.Success -> {
                    topBarTitle = currentRoomDetails.roomDetails.name
                    topBarSubtitle = currentRoomDetails.roomDetails.description ?: ""
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
                onRetry = viewModel::fetchRoomDetails,
                onEditClick = onEditClick,
                onCalendarClick = onCalendarClick
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            RoommatesSection(
                roommatesUiState = roommatesUiState,
                onAdd = onAddRoommateClick,
                onRetry = viewModel::fetchRoomMembers,
                modifier = Modifier
            )

            // Recent Bills Section
            RecentBillsSection(
                billsUiState = billsUiState,
                roommatesUiState = roommatesUiState,
                currentUserId = currentUserIdInt,
                onAddBill = onAddBillClick,
                onPayBill = onFinanceManagerClick,
                onViewAllBills = onFinanceManagerClick,
                onRetry = viewModel::fetchBillsForSummary,
                modifier = Modifier
            )
            // Upcoming Tasks Section
            UpcomingTasksSection(
                tasksUiState = tasksUiState,
                roommatesUiState = roommatesUiState,
                currentUserId = currentUserIdString,
                onAdd = onAddTaskClick,
                onToggleDone = { task, newStatus ->
                    viewModel.updateTaskStatus(task, newStatus)
                },
                onViewAll = onViewTasksClick,
                onRetry = viewModel::fetchTasks,
                modifier = Modifier.weight(1f)
            )
        }
    }
}