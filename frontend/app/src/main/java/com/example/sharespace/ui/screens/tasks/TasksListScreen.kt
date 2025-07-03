package com.example.sharespace.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    onAddTaskClick: () -> Unit = {},
) {
    val taskSummary = listOf(
        "Roommate 1 - 2/5 Complete",
        "Roommate 2 - 6/6 Complete",
        "Roommate 3 - 32/23 Complete",
        "Roommate 4 - 1/9 Complete",
    )

    val inProgressTasks = listOf(
        TaskData("Take out trash", "IN-PROGRESS", listOf("John", "Bob", "Katherine")),
        TaskData("Make cookies", "ASSIGNED", listOf("Jack")),
        TaskData("Cook dinner", "TO-DO", listOf("Simon")),
    )

    val completedTasks = listOf(
        TaskData("Walk The Demon", "COMPLETED", listOf("Jack", "Bob")),
        TaskData("Take Out Trash", "COMPLETED", listOf("Bob")),
        TaskData("Walk the dog", "COMPLETED", listOf("Simon")),
        TaskData("Prep meal", "COMPLETED", listOf("Simon")),
        TaskData("Buy groceries", "COMPLETED", listOf("Bob")),
        TaskData("Make cake", "COMPLETED", listOf("Bob")),
        TaskData("More groceries", "COMPLETED", listOf("Bob")),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("< Task Overview") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddTaskClick() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Task Summary
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.LightGray, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("53 Tasks", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Last 30d", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    taskSummary.forEach {
                        Text(it, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // In Progress Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Upcoming/In Progress Tasks (${inProgressTasks.size})", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { onAddTaskClick() }) {
                    Text("+Add Task")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(inProgressTasks) { task ->
                    TaskCard(task = task)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Completed Tasks
            Text("Completed Tasks (${completedTasks.size})", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            var showAllCompleted by remember { mutableStateOf(false) }
            val maxCollapsedTasks = 3
            val maxUncollapsedTasks = 5
            val hasMoreTasks = completedTasks.size > maxCollapsedTasks

            val displayedTasks = when {
                !showAllCompleted -> completedTasks.take(maxCollapsedTasks)
                completedTasks.size <= maxUncollapsedTasks -> completedTasks
                else -> completedTasks
            }

            if (showAllCompleted && completedTasks.size > maxUncollapsedTasks) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 240.dp) // Scrollable if more than 5
                        .verticalScroll(rememberScrollState())
                ) {
                    displayedTasks.forEach { task ->
                        CompletedTaskRow(task = task)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Column {
                    displayedTasks.forEach { task ->
                        CompletedTaskRow(task = task)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (hasMoreTasks) {
                Button(
                    onClick = { showAllCompleted = !showAllCompleted },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (showAllCompleted) "View Less" else "View More")
                }
            }



        }
    }
}

@Composable
fun TaskCard(task: TaskData) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEFEF))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(task.title, fontWeight = FontWeight.Bold)
            Text(task.status, fontWeight = FontWeight.Medium)
            Text(task.assignees.joinToString(", "), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Edit logic */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Edit Task")
            }
        }
    }
}

@Composable
fun CompletedTaskRow(task: TaskData) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(task.title, fontWeight = FontWeight.Bold)
            Text(task.assignees.joinToString(", "), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.Edit, contentDescription = "Edit Task")
    }
}

data class TaskData(
    val title: String,
    val status: String,
    val assignees: List<String>
)
