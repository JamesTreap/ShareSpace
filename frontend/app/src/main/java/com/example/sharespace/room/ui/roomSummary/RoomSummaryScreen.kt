@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sharespace.room.ui.roomSummary

// import androidx.compose.material.icons.filled.Person // Not used directly in this screen
// import com.example.sharespace.room.ui.roomSummary.components.CalendarSection // Keep if re-added
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
) {
    val roomDetailsState: RoomDetailsUiState = viewModel.roomDetailsUiState
    val roommatesUiState: RoomSummaryRoommatesUiState = viewModel.roommatesUiState
    val tasksUiState: TasksUiState = viewModel.tasksUiState
    val billsUiState: BillsUiState = viewModel.billsUiState

    // Collect both String and Int versions of currentUserId
    val currentUserIdString: String? by viewModel.currentUserIdString.collectAsState()
    val currentUserIdInt: Int? by viewModel.currentUserIdInt.collectAsState()

    val calendarUiState by viewModel.calendarUiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedRoommates by viewModel.selectedRoommates.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current // Get the lifecycle owner

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // This block will execute when the lifecycle is RESUMED
            // and cancel when it's PAUSED.
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
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(vertical = 16.dp)
                .fillMaxSize()
        ) {
//            // Add Finance Manager Button here
//            Button(
//                onClick = onFinanceManagerClick,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 8.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    contentColor = MaterialTheme.colorScheme.onPrimary
//                ),
//                shape = RoundedCornerShape(12.dp),
//                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Star,
//                    contentDescription = "Finance Manager",
//                    modifier = Modifier.size(20.dp)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = "Finance Manager",
//                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
//                )
//            }
            RoommatesSection(
                roommatesUiState = roommatesUiState,
                onAdd = onAddRoommateClick,
                onRetry = viewModel::fetchRoomMembers,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(16.dp)) // Spacing between sections

            // Recent Bills Section
            RecentBillsSection(
                billsUiState = billsUiState,
                roommatesUiState = roommatesUiState,
                currentUserId = currentUserIdInt, // Pass the Int version
                onAddBill = onAddBillClick,
                onPayBill = onFinanceManagerClick,
                onViewAllBills = onFinanceManagerClick,
                onRetry = viewModel::fetchBillsForSummary, // Retry fetching bills
                modifier = Modifier.padding(horizontal = 0.dp) // Section manages its internal padding
            )

            Spacer(Modifier.height(16.dp)) // Spacing between sections

            // Upcoming Tasks Section
            UpcomingTasksSection(
                tasksUiState = tasksUiState,
                roommatesUiState = roommatesUiState, // Pass roommates state
                currentUserId = currentUserIdString, // Tasks section might use String ID
                onAdd = onAddTaskClick,
                onToggleDone = { task, newStatus ->
                    viewModel.updateTaskStatus(task, newStatus)
                },
                onViewAll = onViewTasksClick,
                onRetry = viewModel::fetchTasks,
                modifier = Modifier
                    .weight(1f)
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
//                modifier = Modifier
//                    .weight(1f)
//                    .padding(horizontal = 12.dp)
//            )
        }
    }
}