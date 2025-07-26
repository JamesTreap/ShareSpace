package com.example.sharespace.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sharespace.ShareSpaceApplication
import com.example.sharespace.core.data.remote.ApiClient
import com.example.sharespace.core.data.repository.dto.tasks.ApiTask
import com.example.sharespace.core.data.repository.dto.users.ApiUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.sharespace.core.ui.components.Avatar
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userSessionRepository = (context.applicationContext as ShareSpaceApplication).container.userSessionRepository
    var token by remember { mutableStateOf<String?>(null) }

    var taskToEdit by remember { mutableStateOf<ApiTask?>(null) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occurs by remember { mutableStateOf("") }
    var repeats by remember { mutableStateOf("") }
    var userList by remember { mutableStateOf<List<ApiUser>>(emptyList()) }
    var assigneeStatuses by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userSessionRepository.userTokenFlow.collect { storedToken ->
            token = storedToken?.let { "Bearer $it" }
        }
    }

    LaunchedEffect(taskId, token) {
        token?.let { authToken ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val roomId = 7

                    val taskRes = ApiClient.apiService.getTasksForRoom(roomId, authToken)
                    val userRes = ApiClient.apiService.getRoomMembers(roomId, authToken)

                    if (taskRes.isSuccessful && userRes.isSuccessful) {
                        val allTasks = taskRes.body() ?: emptyList()
                        val foundTask = allTasks.find { it.id == taskId }

                        val roommates = userRes.body()?.roommates ?: emptyList()

                        withContext(Dispatchers.Main) {
                            userList = roommates
                            if (foundTask != null) {
                                taskToEdit = foundTask
                                title = foundTask.title
                                description = foundTask.description ?: ""
                                date = foundTask.deadline.substringBefore("T")
                                time = foundTask.deadline.substringAfter("T")
                                occurs = foundTask.frequency ?: ""
                                repeats = foundTask.repeat.toString() ?: ""
                                assigneeStatuses = foundTask.statuses
                            } else {
                                errorMessage = "Task not found"
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Failed to fetch data"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        errorMessage = e.message
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = occurs,
                    onValueChange = { occurs = it },
                    label = { Text("Occurs") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = repeats,
                    onValueChange = { repeats = it },
                    label = { Text("Number of repeats") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Assigned Users", fontWeight = FontWeight.Bold)

            userList.forEach { user ->
                val userIdStr = user.id.toString()
                var statusText by remember { mutableStateOf(assigneeStatuses[userIdStr] ?: "TODO") }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Avatar(
                        photoUrl = user.profilePictureUrl,
                        contentDescription = "${user.name}'s avatar",
                        size = 40.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))
                    Text(user.name, modifier = Modifier.weight(1f))

                    OutlinedTextField(
                        value = statusText,
                        onValueChange = {
                            statusText = it
                            assigneeStatuses = assigneeStatuses.toMutableMap().also { map ->
                                map[userIdStr] = it
                            }
                        },
                        modifier = Modifier
                            .width(200.dp)
                            .padding(start = 8.dp),
                        singleLine = true
                    )
                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Text(errorMessage ?: "", color = Color.Red)
            }

            Button(
                onClick = {
                    // Update functionality to be implemented
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C2A8))
            ) {
                Text("Update Task", color = Color.White)
            }
        }
    }
}
