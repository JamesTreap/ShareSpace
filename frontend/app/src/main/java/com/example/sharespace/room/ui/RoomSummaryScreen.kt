@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sharespace.room.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sharespace.core.domain.model.Bill
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.core.domain.model.User
import java.time.format.DateTimeFormatter

@Composable
fun RoomSummaryScreen(
    viewModel: RoomSummaryViewModel = viewModel(factory = RoomSummaryViewModel.Factory),
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
        // Main layout is a Column. If content overflows screen height, it will NOT scroll by default.
        // If scrolling is needed for the entire screen despite fixed sections,
        // add .verticalScroll(rememberScrollState()) to this Column.
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
            // .verticalScroll(rememberScrollState()) // Add this if the WHOLE Column needs to scroll
        ) {
            // Finance Manager Button (Fixed part of the Column)
            Button(
                onClick = onFinanceManagerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = "Finance Manager")
            }

            // Recent Bills Section (Fixed part of the Column, internal LazyRow for scrolling)
            RecentBillsSection(
                bills = bills,
                onPay = viewModel::payBill,
                onViewAll = onViewBillsClick
            )

            // Roommates Section (Fixed part of the Column, internal LazyRow for scrolling)
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

            // Upcoming Tasks Section (Fixed part of the Column, internal LazyColumn for scrolling)
            // The LazyColumn inside UpcomingTasksSection should have a defined height or use weight
            // if the parent Column does not scroll and this section needs to take remaining space and scroll.
            // For simplicity here, assuming it takes natural height up to a point, then its internal LazyColumn scrolls.
            UpcomingTasksSection(
                tasks = tasks,
                onToggleDone = viewModel::toggleTaskDone,
                onAdd = onAddTaskClick,
                onViewAll = onViewTasksClick,
                // If this section should take all remaining vertical space and scroll internally:
                // modifier = Modifier.weight(1f).fillMaxHeight() // Pass this modifier to UpcomingTasksSection
            )

            // Add Spacer at the bottom if the main Column is scrollable and you want to ensure
            // the last element isn't glued to the bottom navigation bar (if any).
            // If the main Column is not scrollable, this Spacer might push content off-screen
            // if the content is too tall.
            // Spacer(Modifier.height(16.dp)) // Optional: for bottom padding if main Column scrolls
        }
    }
}


@Composable
fun RoomSummaryTopAppBar(
    address: String,
    subtitle: String,
    onNavigateBack: () -> Unit,
    showRetry: Boolean = false,
    onRetry: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(text = address, style = MaterialTheme.typography.titleLarge)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (showRetry) {
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    )
}

@Composable
fun RecentBillsSection(
    bills: List<Bill>, onPay: (Bill) -> Unit, onViewAll: () -> Unit
) {
    // This Column is a fixed part of the main screen layout
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = "Recent Bills", actionText = "View All", onAction = onViewAll)
        if (bills.isEmpty()) {
            Text(
                "No recent bills.",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
            return@Column // Use return@Column to exit this composable's Column
        }
        // LazyRow provides horizontal scrolling for bills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(bills) { bill ->
                ElevatedCard(modifier = Modifier.width(180.dp)) { // Fixed width for bill cards
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(bill.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("$${bill.amount}", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(bill.subtitle, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { onPay(bill) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Pay User")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String, actionText: String? = null, actionIcon: ImageVector? = null, onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        when {
            actionText != null -> TextButton(onClick = onAction) { Text(actionText) }
            actionIcon != null -> IconButton(onClick = onAction) {
                Icon(actionIcon, contentDescription = title)
            }
        }
    }
}

@Composable
fun RoommatesSection(
    roommates: List<User>, onAdd: () -> Unit, onViewAll: () -> Unit
) {
    // This Column is a fixed part of the main screen layout
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = "Roommates", actionText = "View All", onAction = onViewAll)
        if (roommates.isEmpty()) {
            Row( // Using Row to align text and add button
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("No roommates yet. Add one!", modifier = Modifier.weight(1f))
                OutlinedCard(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(onClick = onAdd),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Add, contentDescription = "Add roommate")
                    }
                }
            }
            return@Column
        }
        // LazyRow provides horizontal scrolling for roommates
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(roommates) { user ->
                Avatar(
                    photoUrl = user.photoUrl, contentDescription = "Avatar of ${user.name}"
                )
            }
            item { // Add roommate button at the end
                OutlinedCard(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(onClick = onAdd),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Add, contentDescription = "Add roommate")
                    }
                }
            }
        }
    }
}

@Composable
fun Avatar(
    photoUrl: String?, contentDescription: String? = null, size: Dp = 56.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null && photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(0.7f)
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
    modifier: Modifier = Modifier // Added modifier parameter
) {
    // This Column is a fixed part of the main screen layout
    Column(modifier = modifier.fillMaxWidth()) { // Apply passed modifier
        SectionHeader(
            title = "Upcoming Tasks",
            actionText = "+ Add Task",
            onAction = onAdd
        )
        if (tasks.isEmpty()) {
            Text(
                "No upcoming tasks.",
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Button( // "View all" button consistent with your structure
                onClick = onViewAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Text("View all / Add Task")
            }
            Spacer(Modifier.height(8.dp))
            return@Column
        }

        // LazyColumn provides vertical scrolling for tasks IF it has a constrained height.
        // If the parent Column (RoomSummaryScreen's main Column) does not scroll,
        // this LazyColumn needs a specific height or Modifier.weight(1f) to function correctly
        // and not try to take infinite height.
        LazyColumn(
            // To make this LazyColumn scroll within its section if the main screen Column is fixed:
            // 1. Give it a fixed height: .height(someDpValue.dp)
            // 2. Or if it's the last item meant to take remaining space:
            //    Pass Modifier.weight(1f) from the parent and ensure parent has defined height or uses fillMaxHeight.
            //    For this example, we'll assume it has enough space or its parent has a scroll.
            //    If main screen Column is NOT scrollable, and this LazyColumn is too tall, UI issues can occur.
            //    A common pattern is Modifier.fillMaxHeight() if it's the only scrollable content in a fixed parent.
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // EXAMPLE: Constrain height to enable internal scrolling. Adjust as needed.
            // Or remove .height() if this section is inside a scrollable parent,
            // or if you use Modifier.weight(1f) and its parent allows it.
        ) {
            items(tasks) { task ->
                ListItem(
                    headlineContent = { Text(task.title) },
                    supportingContent = {
                        Text(
                            task.dueDate.format(DateTimeFormatter.ofPattern("MMM d | h:mm a"))
                                ?: "No due date"
                        )
                    },
                    leadingContent = {
                        Avatar(
                            photoUrl = null,
                            size = 40.dp,
                            contentDescription = "Task assignment placeholder"
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { onToggleDone(task) }) {
                            Icon(
                                imageVector = if (task.isDone) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = if (task.isDone) "Mark as not done" else "Mark as done"
                            )
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp))
            }

            item { // "View all" button inside the LazyColumn
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

