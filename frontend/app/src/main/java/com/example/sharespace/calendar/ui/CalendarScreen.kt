package com.example.sharespace.calendar.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.calendar.viewmodel.CalendarScreenUiState
import com.example.sharespace.calendar.viewmodel.CalendarViewModel
import com.example.sharespace.core.domain.model.Bill
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.room.ui.roomSummary.components.DateSelector
import displayDateFlexible
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedRoommates by viewModel.selectedRoommates.collectAsState()
    val currentUserId by viewModel.currentUserIdString.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
            )

        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Use your DateSelector component
            DateSelector(
                selectedDate = selectedDate,
                onDateSelected = { newDate -> viewModel.updateSelectedDate(newDate) })
            Spacer(modifier = Modifier.height(12.dp))

            when (val currentUiState = uiState) {
                is CalendarScreenUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        Text("Loading calendar data...", modifier = Modifier.padding(top = 8.dp))
                    }
                }

                is CalendarScreenUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Error: ${currentUiState.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.retryDataFetch() }) { Text("Retry") }
                    }
                }

                is CalendarScreenUiState.Success -> {
                    // Use your RoommateFilter component
                    if (currentUiState.roommates.isNotEmpty()) {
                        RoommateFilter( // Your custom RoommateFilter
                            roommates = currentUiState.roommates,
                            selectedRoommates = selectedRoommates,
                            onSelectionChanged = { newSelection ->
                                viewModel.updateSelectedRoommates(newSelection)
                            })
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    val filteredBills = if (selectedRoommates.isEmpty()) {
                        currentUiState.bills
                    } else {
                        currentUiState.bills.filter { bill ->
                            bill.metadata?.users?.any { userDue ->
                                selectedRoommates.contains(userDue.userId)
                            } == true || selectedRoommates.contains(bill.payerUserId)
                        }
                    }
                    val tasksToDisplay = currentUiState.tasks // No roommate filter for tasks yet

                    if (filteredBills.isEmpty() && tasksToDisplay.isEmpty()) {
                        Text(
                            "No events scheduled for ${
                                selectedDate.format(
                                    DateTimeFormatter.ofLocalizedDate(
                                        FormatStyle.MEDIUM
                                    )
                                )
                            }" + if (selectedRoommates.isNotEmpty()) " for the selected roommates." else ".",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            if (filteredBills.isNotEmpty()) {
                                item { SectionHeader(title = "Bills Due") }
                                items(filteredBills, key = { "bill-${it.id}" }) { bill ->
                                    BillRow(bill = bill, modifier = Modifier.fillMaxWidth())
                                }
                            }
                            if (tasksToDisplay.isNotEmpty()) {
                                item {
                                    if (filteredBills.isNotEmpty()) Spacer(
                                        modifier = Modifier.height(
                                            16.dp
                                        )
                                    )
                                    SectionHeader(title = "Tasks")
                                }
                                items(tasksToDisplay, key = { "task-${it.id}" }) { task ->
                                    CalendarTaskRow(
                                        task = task,
                                        currentUserId = currentUserId,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier.padding(vertical = 8.dp)
    )
}


@Composable
fun BillRow(bill: Bill, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scheduledDateText = remember(bill.scheduledDate, context) {
        bill.scheduledDate?.let { date ->
            "Scheduled: ${displayDateFlexible(date, context)}"
        } ?: "Not scheduled"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bill.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = scheduledDateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "$${"%.2f".format(bill.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CalendarTaskRow(task: Task, currentUserId: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val deadlineText = remember(task.deadline, context) {
        task.deadline?.let { deadlineDate ->
            "Deadline: ${displayDateFlexible(deadlineDate, context)}"
        } ?: "Deadline: Not set"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = deadlineText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                task.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
