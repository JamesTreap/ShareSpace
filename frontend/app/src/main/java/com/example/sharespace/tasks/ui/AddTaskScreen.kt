package com.example.sharespace.ui.screens.tasks

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sharespace.ShareSpaceApplication
import com.example.sharespace.core.data.remote.ApiClient
import com.example.sharespace.core.data.remote.Assignee
import com.example.sharespace.core.data.remote.CreateTaskRequest
import com.example.sharespace.core.data.repository.dto.ApiUser
import com.example.sharespace.core.ui.components.Avatar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    roomId: Int = 7,
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val userSessionRepository = (context.applicationContext as ShareSpaceApplication).container.userSessionRepository

    var token by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occurs by remember { mutableStateOf("") }
    var repeats by remember { mutableStateOf("") }
    var userList by remember { mutableStateOf<List<ApiUser>>(emptyList()) }
    var selectedUserIds by remember { mutableStateOf(setOf<Int>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userSessionRepository.userTokenFlow.collect { storedToken ->
            token = storedToken?.let { "Bearer $it" }
        }
    }

    LaunchedEffect(token) {
        token?.let { authToken ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiClient.apiService.getRoomMembers(roomId, authToken)
                    if (response.isSuccessful) {
                        val roommates = response.body()?.roommates ?: emptyList()
                        withContext(Dispatchers.Main) {
                            userList = roommates
                            selectedUserIds = roommates.map { it.id }.toSet()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Failed to fetch roommates"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        errorMessage = e.localizedMessage
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Task") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() }) {
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
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time") },
                    placeholder = { Text("HH:MM") },
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
            Text("Assign To", fontWeight = FontWeight.Bold)

            userList.forEach { user ->
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
                    Checkbox(
                        checked = selectedUserIds.contains(user.id),
                        onCheckedChange = {
                            selectedUserIds = if (it) {
                                selectedUserIds + user.id
                            } else {
                                selectedUserIds - user.id
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Text(errorMessage ?: "", color = Color.Red)
            }

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val authToken = token ?: return@launch  // prevent call with null token

                            val parsedDate =
                                LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val parsedTime =
                                LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                            val deadline = parsedDate.toString()
                            val dueTime = parsedTime.toString()

                            val request = CreateTaskRequest(
                                title = title,
                                date = "${deadline}T$dueTime",
                                description = description,
                                assignees = selectedUserIds.map { Assignee(it, "todo") },
                                frequency = occurs,
                                repeat = repeats
                            )

                            val gson = Gson()
                            Log.d("TASK_JSON", gson.toJson(request))


                            val res = ApiClient.apiService.createTask(
                                roomId,
                                request = request,
                                token = authToken
                            )
                            if (res.isSuccessful) {
                                withContext(Dispatchers.Main) {
                                    onNavigateBack?.invoke()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Failed to create task: ${res.code()}"
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = e.localizedMessage
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C2A8))
            ) {
                Text("Create Task", color = Color.White)
            }
        }
    }
}

