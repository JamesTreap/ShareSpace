package com.example.sharespace.calendar.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.calendar.viewmodel.CalendarScreenUiState
import com.example.sharespace.calendar.viewmodel.CalendarViewModel
import com.example.sharespace.core.domain.model.Bill
import com.example.sharespace.core.domain.model.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory),
    onNavigateBack: () -> Unit,
    // Add navigation callbacks if clicking on a task/bill should go somewhere
    // onNavigateToTaskDetails: (String) -> Unit,
    // onNavigateToBillDetails: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") }, navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Date Picker / Selector
            // TODO: Implement a Date Picker (e.g., showDatePicker dialog or a custom row of dates)
            // For now, just display selected date and allow basic navigation
            DateSelector(
                selectedDate = selectedDate,
                onDateChange = { newDate -> viewModel.updateSelectedDate(newDate) })
            Spacer(modifier = Modifier.height(16.dp))

            when (val currentUiState = uiState) {
                is CalendarScreenUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text("Loading calendar data...")
                    }
                }

                is CalendarScreenUiState.Success -> {
                    if (currentUiState.tasks.isEmpty() && currentUiState.bills.isEmpty()) {
                        Text(
                            "No events scheduled for ${
                                selectedDate.format(
                                    DateTimeFormatter.ofLocalizedDate(
                                        FormatStyle.MEDIUM
                                    )
                                )
                            }.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        CalendarEventsList(
                            tasks = currentUiState.tasks, bills = currentUiState.bills
                        )
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
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchCalendarData() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateSelector(
    selectedDate: LocalDate, onDateChange: (LocalDate) -> Unit, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { onDateChange(selectedDate.minusDays(1)) }) {
            Text("Prev")
        }
        Text(
            text = selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
            style = MaterialTheme.typography.titleMedium
        )
        // TODO: Consider showing a proper DatePickerDialog onClick here
        // For now, this text is not clickable for picking a specific date from a calendar.
        Button(onClick = { onDateChange(selectedDate.plusDays(1)) }) {
            Text("Next")
        }
    }
}


@Composable
fun CalendarEventsList(
    tasks: List<Task>, bills: List<Bill>, modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (tasks.isNotEmpty()) {
            item {
                Text(
                    "Tasks",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(tasks, key = { "task-${it.id}" }) { task ->
                CalendarTaskItem(task = task)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (bills.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(if (tasks.isNotEmpty()) 16.dp else 0.dp))
                Text(
                    "Bills Due",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(bills, key = { "bill-${it.id}" }) { bill ->
                CalendarBillItem(bill = bill)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CalendarTaskItem(task: Task, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            task.description?.let {
                if (it.isNotBlank()) {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                "Deadline: ${task.deadline.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))}",
                style = MaterialTheme.typography.bodySmall
            )
            // TODO: Display assignees and their statuses if needed
        }
    }
}

@Composable
fun CalendarBillItem(bill: Bill, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(bill.title, style = MaterialTheme.typography.titleMedium)
            Text(
                "Amount: $${"%.2f".format(bill.amount)}",
                style = MaterialTheme.typography.bodyMedium
            )
            bill.deadline?.let {
                Text(
                    "Due: ${it.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // TODO: Display who it's owed to / who paid if needed
        }
    }
}

