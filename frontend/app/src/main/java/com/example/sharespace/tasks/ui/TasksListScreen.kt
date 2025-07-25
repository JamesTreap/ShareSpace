package com.example.sharespace.ui.screens.tasks

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharespace.core.data.remote.ApiClient
import com.example.sharespace.core.data.repository.dto.tasks.ApiTask
import com.example.sharespace.core.ui.components.NavigationHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    onNavigateBack: () -> Unit,
    onAddTaskClick: () -> Unit = {},
    onEditTaskClick: (Int) -> Unit
)
 {
    var taskSummary by remember { mutableStateOf(listOf<String>()) }
    var allTasks by remember { mutableStateOf(listOf<ApiTask>()) }
    var inProgressTasks by remember { mutableStateOf<List<TaskData>>(emptyList()) }
    var completedTasks by remember { mutableStateOf<List<TaskData>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch task data from API
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val roomId = 7 // ← Replace this with actual room ID
                val token =
                    "Bearer your_token_here" // ← Replace with real token or maybe just ignore it

                val response = ApiClient.apiService.getTasksForRoom(roomId, token)
                Log.d("TASKS", "Fetched tasks: ${response.body()}")

                if (response.isSuccessful) {
                    val apiTasks = response.body() ?: emptyList()
                    val today = LocalDate.now()
                    val thirtyDaysAgo = today.minusDays(30)

                    // Filter tasks that are within the last 30 days
                    val filteredTasks = apiTasks.filter { task ->
                        try {
                            val deadline = LocalDate.parse(task.deadline.substringBefore("T"))
                            !deadline.isAfter(today) && !deadline.isBefore(thirtyDaysAgo)
                        } catch (e: Exception) {
                            false
                        }
                    }
                    allTasks = filteredTasks

                    // Extract all unique user IDs from the task statuses
                    val userIds =
                        filteredTasks.flatMap { it.statuses.keys }.mapNotNull { it.toIntOrNull() }
                            .toSet()

                    // Fetch user details for each ID
                    val userIdToName = mutableMapOf<String, String>()

                    //Count how many tasks each user has in total & how many they completed
                    val userTaskMap =
                        mutableMapOf<String, Pair<Int, Int>>()

                    for (task in filteredTasks) {
                        for ((userId, status) in task.statuses) {
                            val (completed, total) = userTaskMap[userId] ?: (0 to 0)
                            val newCompleted =
                                if (status == "COMPLETE") completed + 1 else completed
                            userTaskMap[userId] = (newCompleted to total + 1)
                        }
                    }
                    // After fetching user details
                    for (id in userIds) {
                        try {
                            val userResp = ApiClient.apiService.getUserDetailsById(id, token)
                            if (userResp.isSuccessful) {
                                userResp.body()?.let { userIdToName[id.toString()] = it.name }
                            }
                        } catch (_: Exception) {
                        }
                    }
                    // Now that names are available, convert summary
                    val generatedSummary = userTaskMap.entries.map { (userId, pair) ->
                        val name = userIdToName[userId] ?: "User $userId"
                        "$name - ${pair.first}/${pair.second} Complete"
                    }



                    for (id in userIds) {
                        try {
                            val userResp = ApiClient.apiService.getUserDetailsById(id, token)
                            if (userResp.isSuccessful) {
                                userResp.body()?.let { userIdToName[id.toString()] = it.name }
                            }
                        } catch (_: Exception) {
                        }
                    }

                    // Map tasks to TaskData with proper status and real user names
                    val mapped = filteredTasks.map { task ->
                        val statuses = task.statuses
                        val statusValues = statuses.values.toSet()

                        val finalStatus = when {
                            statusValues.isEmpty() -> "ASSIGNED" // Default if no status
                            statusValues.all { it == "COMPLETE" } -> "COMPLETE"
                            statusValues.any { it == "IN-PROGRESS" } -> "IN-PROGRESS"
                            else -> "ASSIGNED"
                        }

                        val assignees = statuses.keys.map { uid ->
                            userIdToName[uid] ?: "User $uid"
                        }

                        TaskData(
                            id = task.id,
                            title = task.title,
                            status = finalStatus,
                            assignees = assignees
                        )
                    }

                    inProgressTasks = mapped.filter { it.status != "COMPLETE" }
                    completedTasks = mapped.filter { it.status == "COMPLETE" }
                    taskSummary = generatedSummary
                } else {
                    error = "API Error: ${response.code()}"
                }

            } catch (e: IOException) {
                error = "Network Error: ${e.message}"
            } catch (e: HttpException) {
                error = "HTTP Error: ${e.message}"
            } catch (e: Exception) {
                error = "Unexpected Error: ${e.message}"
            }
        }
    }
    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Task Overview",
                onNavigateBack = onNavigateBack
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
                        Text(
                            "${allTasks.size} Tasks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
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
                Text(
                    "Upcoming/In Progress Tasks (${inProgressTasks.size})",
                    fontWeight = FontWeight.Bold
                )
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
                    TaskCard(task = task, onEditClick = onEditTaskClick)                }
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
fun TaskCard(task: TaskData, onEditClick: (Int) -> Unit) {
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
            Button(onClick = { onEditClick(task.id) }, modifier = Modifier.fillMaxWidth()) {
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
    val id: Int,
    val title: String,
    val status: String,
    val assignees: List<String>
)


