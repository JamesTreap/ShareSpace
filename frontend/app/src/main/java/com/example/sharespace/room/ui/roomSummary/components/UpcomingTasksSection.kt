package com.example.sharespace.room.ui.roomSummary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.core.ui.components.AvatarSquare
import com.example.sharespace.room.viewmodel.TasksUiState
import java.time.format.DateTimeFormatter

@Composable
fun UpcomingTasksSection(
    tasksUiState: TasksUiState,
    onAdd: () -> Unit,
    onToggleDone: (Task, newStatus: String) -> Unit,
    onViewAll: () -> Unit,
    onRetry: () -> Unit,
    currentUserId: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SectionHeader(
            title = "Upcoming Tasks",
            actionText = "+ Add Task",
            onAction = onAdd,
            modifier = Modifier.fillMaxWidth()
        )

        when (tasksUiState) {
            is TasksUiState.Loading -> Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is TasksUiState.Error -> ErrorMini(onRetry)

            is TasksUiState.Empty -> EmptyMini(onAdd)

            is TasksUiState.Success -> {
                val tasks = tasksUiState.tasks
                if (tasks.isEmpty()) {
                    EmptyMini(onAdd)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            val isDone = task.statuses[currentUserId]?.equals(
                                "complete", ignoreCase = true
                            ) == true
                            TaskRow(
                                task = task, isDone = isDone, onToggle = {
                                    val newStatus = if (isDone) "pending" else "complete"
                                    onToggleDone(task, newStatus)
                                })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onViewAll,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("View All") }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: Task, isDone: Boolean, onToggle: () -> Unit
) {
    val dateText =
        task.deadline.format(DateTimeFormatter.ofPattern("MMMM d | h:mm a")) ?: "No due date"

    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarSquare(
            photoUrl = "pfp1", size = 56.dp, cornerRadius = 8.dp
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Text(
                dateText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(12.dp))

        // Checkbox replaces TaskCheckBadge
        Box(
            modifier = Modifier.size(48.dp),   // preserves 48dp touch target
            contentAlignment = Alignment.Center
        ) {
            Checkbox(
                checked = isDone, onCheckedChange = { _ -> onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                ), modifier = Modifier.semantics { this.role = Role.Checkbox })
        }
    }
}


@Composable
private fun ErrorMini(onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Failed to load tasks.", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun EmptyMini(onAdd: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No upcoming tasks.")
        Spacer(Modifier.height(8.dp))
        Button(onClick = onAdd) { Text("Add Task") }
    }
}
