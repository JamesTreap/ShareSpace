package com.example.sharespace.room.ui.roomSummary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.data.repository.dto.users.ApiUser
import com.example.sharespace.core.domain.model.User
import com.example.sharespace.room.viewmodel.CalendarUiState
import java.time.LocalDate

@Composable
fun CalendarSection(
    calendarUiState: CalendarUiState,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    roommates: List<User>, // Changed from List<ApiUser> to List<User>
    selectedRoommates: Set<Int>,
    onRoommateSelectionChanged: (Set<Int>) -> Unit,
    currentUserId: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
)   {
    Column(modifier = modifier) {
        SectionHeader(
            title = "Calendar",
            actionText = null,
            onAction = { },
            modifier = Modifier.fillMaxWidth()
        )

        // Date Selector
        DateSelector(selectedDate, onDateSelected)

        // Roommate Filter
        RoommateFilter(roommates, selectedRoommates, onRoommateSelectionChanged)

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar Items
        when (calendarUiState) {
            is CalendarUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp), contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is CalendarUiState.Error -> {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(calendarUiState.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRetry) { Text("Retry") }
                }
            }

            is CalendarUiState.Success -> {
                // Filter bills based on selected roommates
                val filteredBills = if (selectedRoommates.isEmpty()) {
                    calendarUiState.bills
                } else {
                    calendarUiState.bills.filter { bill ->
                        bill.metadata?.users?.any { user ->
                            selectedRoommates.contains(user.userId.toInt())
                        } == true
                    }
                }

                if (filteredBills.isEmpty() && calendarUiState.tasks.isEmpty()) {
                    Text(
                        "No calendar items for this date",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Bills section
                        if (filteredBills.isNotEmpty()) {
                            item {
                                Text(
                                    "Bills",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(filteredBills, key = { it.id }) { bill ->
                                BillRow(bill)
                            }
                        }

                        // Tasks section
                        if (calendarUiState.tasks.isNotEmpty()) {
                            item {
                                Text(
                                    "Tasks",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(calendarUiState.tasks, key = { it.id }) { task ->
                                CalendarTaskRow(
                                    task = task, currentUserId = currentUserId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}