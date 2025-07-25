package com.example.sharespace.room.ui.roomSummary.components

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.domain.model.Task
import com.example.sharespace.core.ui.components.Avatar
import java.time.format.DateTimeFormatter

@Composable
fun UpcomingTasksSection(
    tasks: List<Task>,
    onAdd: () -> Unit,
    onToggleDone: (Task) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Upcoming Tasks", actionText = "+ Add Task", onAction = onAdd
        )
        if (tasks.isEmpty()) {
            Text(
                "No upcoming tasks.",
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Button( // "View all" button consistent with your structure
                onClick = onViewAll, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Text("View all / Add Task")
            }
            Spacer(Modifier.height(8.dp))
            return@Column
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            items(tasks) { task ->
                ListItem(headlineContent = { Text(task.title) }, supportingContent = {
                    Text(
                        task.dueDate.format(DateTimeFormatter.ofPattern("MMM d | h:mm a"))
                            ?: "No due date"
                    )
                }, leadingContent = {
                    Avatar(
                        photoUrl = null,
                        size = 40.dp,
                        contentDescription = "Task assignment placeholder"
                    )
                }, trailingContent = {
                    IconButton(onClick = { onToggleDone(task) }) {
                        Icon(
                            imageVector = if (task.isDone) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = if (task.isDone) "Mark as not done" else "Mark as done"
                        )
                    }
                })
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp))
            }

            item {
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