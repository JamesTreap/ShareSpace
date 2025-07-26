package com.example.sharespace.ui.screens.tasks

import android.util.Log
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
import com.example.sharespace.core.ui.components.StyledTextField
import com.example.sharespace.core.ui.components.StyledCircleLoader
import com.example.sharespace.core.data.repository.dto.tasks.ApiAssignee
import com.example.sharespace.core.data.repository.dto.tasks.ApiUpdateTaskRequest
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userSessionRepository = (context.applicationContext as ShareSpaceApplication).container.userSessionRepository
    var token by remember { mutableStateOf<String?>(null) }
    var roomId by remember { mutableStateOf<Int?>(null) }
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
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        userSessionRepository.userTokenFlow.collect { storedToken ->
            token = storedToken?.let { "Bearer $it" }
        }
    }
    LaunchedEffect(Unit) {
        userSessionRepository.activeRoomIdFlow.collect { storedRoomId ->
            roomId = storedRoomId
        }
    }

    LaunchedEffect(taskId, token) {
        token?.let { authToken ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    println("TOKEN: $token")
                    println("ROOMID: $roomId")

                    val taskRes = roomId?.let { ApiClient.apiService.getTasksForRoom(it, authToken) }
                    val userRes = roomId?.let { ApiClient.apiService.getRoomMembers(it, authToken) }

                    if (taskRes != null) {
                        if (userRes != null) {
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
                                        repeats = (foundTask.repeat ?: "").toString()
                                        assigneeStatuses = foundTask.statuses
                                    } else {
                                        errorMessage = "Task not found"
                                    }
                                    isLoading = false
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Failed to fetch data"
                                }
                            }
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
            StyledTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                StyledTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                StyledTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time") },
                    modifier = Modifier.weight(1f)
                )
            }

            StyledTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                StyledTextField(
                    value = occurs,
                    onValueChange = { occurs = it },
                    label = { Text("Occurs") },
                    enabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                StyledTextField(
                    value = repeats,
                    onValueChange = { repeats = it },
                    label = { Text("Number of repeats") },
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                StyledCircleLoader(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Text("Assigned Users", fontWeight = FontWeight.Bold)

                userList.forEach { user ->
                    val userIdStr = user.id.toString()
                    var statusText by remember { mutableStateOf(assigneeStatuses[userIdStr] ?: "NOT ASSIGNED") }

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

                        StyledTextField(
                            value = statusText,
                            onValueChange = {
                                statusText = it
                                assigneeStatuses = assigneeStatuses.toMutableMap().also { map ->
                                    map[userIdStr] = it
                                }
                            },
                            enabled = statusText != "NOT ASSIGNED",
                            modifier = Modifier
                                .width(200.dp)
                                .padding(start = 8.dp),
                            singleLine = true

                            )
                    }
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Text(errorMessage ?: "", color = Color.Red)
            }

            Button(
                onClick = {
                    val finalToken = token
                    val finalRoomId = roomId
                    val taskIdFinal = taskId

                    if (finalToken != null && finalRoomId != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val assigneeList = assigneeStatuses.map { (userId, status) ->
                                    ApiAssignee(
                                        userId = userId,
                                        status = status.lowercase()
                                    )
                                }

                                val finalRrequest = ApiUpdateTaskRequest(
                                    title = title,
                                    date = "${date}T${time}",
                                    description = description,
                                    assignees = assigneeList
                                )

//                                Gson().toJson(finalRrequest)
//                                Log.d("UpdateTask", "Payload: $finalRrequest")
//                                Log.d("UpdateTask", "Token: $token")
//                                Log.d("UpdateTask", "taskIdFinal: $taskIdFinal")

                                val response = ApiClient.apiService.updateTask(
                                    taskId = taskIdFinal,
                                    token = finalToken,
                                    request = finalRrequest
                                )

                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful) {
                                        onNavigateBack()
                                    } else {
                                        errorMessage = "Failed to update task: ${response.code()}"
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Error: ${e.localizedMessage}"
                                }
                            }
                        }
                    }
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
