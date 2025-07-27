package com.example.sharespace.room.ui.roomSummary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.core.ui.components.Avatar
import com.example.sharespace.room.viewmodel.TasksUiState
import java.time.format.DateTimeFormatter

@Composable
fun UpcomingTasksSection(
    tasksUiState: TasksUiState, // Changed to accept UiState
    onAdd: () -> Unit,
    onToggleDone: (Task, newStatus: String) -> Unit, // Modified for clarity
    onViewAll: () -> Unit,
    onRetry: () -> Unit, // Add a retry callback
    currentUserId: String?, // Pass current user ID for task status determination
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Upcoming Tasks", actionText = "+ Add Task", onAction = onAdd
        )

        when (tasksUiState) {
            is TasksUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Maintain height during loading
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is TasksUiState.Success -> {
                val tasks = tasksUiState.tasks
                if (tasks.isEmpty()) { // This case is now handled by TasksUiState.Empty
                    // This specific 'if' block might be redundant if TasksUiState.Empty is handled below
                    // However, keeping it for now in case Success can have an empty list
                    // but usually TasksUiState.Empty would be preferred.
                    Text(
                        "No upcoming tasks.",
                        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onViewAll, // Or onAdd if that's more appropriate
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Text("View all / Add Task")
                    }
                    Spacer(Modifier.height(8.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // Fixed height for the list
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            val currentUserStatus = task.statuses[currentUserId]
                            val isDone =
                                currentUserStatus?.equals("complete", ignoreCase = true) == true

                            ListItem(
                                headlineContent = { Text(task.title) },
                                supportingContent = {
                                    Text(
                                        task.deadline?.format(DateTimeFormatter.ofPattern("MMM d | h:mm a"))
                                            ?: "No due date"
                                    )
                                },
                                leadingContent = {
                                    // You might want to show assignee's avatar if available
                                    Avatar(
                                        photoUrl = null, // Placeholder or actual assignee photo
                                        size = 40.dp,
                                        contentDescription = "Task assignment placeholder"
                                    )
                                },
                                trailingContent = {
                                    IconButton(onClick = {
                                        val newStatus = if (isDone) "pending" else "complete"
                                        onToggleDone(task, newStatus)
                                    }) {
                                        Icon(
                                            imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                            contentDescription = if (isDone) "Mark as not done" else "Mark as done",
                                            tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp
                                )
                            )
                        }
                        item { // "View all tasks" button at the end of the list
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

            is TasksUiState.Empty -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp) // Give some fixed height
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No upcoming tasks. Create one!")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onAdd) {
                        Text("Add Task")
                    }
                }
            }

            is TasksUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp) // Give some fixed height
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Failed to load tasks.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}