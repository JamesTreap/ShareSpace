package com.example.sharespace.ui.screens.tasks

import android.util.Log
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.sharespace.core.data.repository.dto.tasks.ApiAssignee
import com.example.sharespace.core.data.repository.dto.tasks.ApiCreateTaskRequest
import com.example.sharespace.core.data.repository.dto.users.ApiUser
import com.example.sharespace.core.ui.components.Avatar
import com.example.sharespace.core.ui.components.ButtonType
import com.example.sharespace.core.ui.components.DatePickerSelector
import com.example.sharespace.core.ui.components.StyledButton
import com.example.sharespace.core.ui.components.StyledCheckbox
import com.example.sharespace.core.ui.components.StyledCircleLoader
import com.example.sharespace.core.ui.components.StyledSelect
import com.example.sharespace.core.ui.components.StyledTextField
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
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val userSessionRepository =
        (context.applicationContext as ShareSpaceApplication).container.userSessionRepository
    var token by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occurs by remember { mutableStateOf("1d") }
    var repeats by remember { mutableStateOf("0") }
    var userList by remember { mutableStateOf<List<ApiUser>>(emptyList()) }
    var selectedUserIds by remember { mutableStateOf(setOf<Int>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var roomId by remember { mutableStateOf<Int?>(null) }
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
    LaunchedEffect(token) {
        token?.let { authToken ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response =
                        roomId?.let { ApiClient.apiService.getRoomMembers(it, authToken) }
                    if (response != null) {
                        if (response.isSuccessful) {
                            val roommates = response.body()?.roommates ?: emptyList()
                            withContext(Dispatchers.Main) {
                                userList = roommates
                                selectedUserIds = roommates.map { it.id }.toSet()
                                isLoading = false
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Failed to fetch roommates"
                            }
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
            TopAppBar(title = { Text("Add Task") }, navigationIcon = {
                IconButton(onClick = { onNavigateBack?.invoke() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            })
        }) { innerPadding ->
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
                DatePickerSelector(
                    selectedDate = date,
                    onDateSelected = { date = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                StyledTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time") },
                    placeholder = { Text("HH:MM") },
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    StyledSelect(
                        options = (0..10).map { it.toString() },
                        label = "Number of repeats",
                        initialSelected = repeats,
                        onOptionSelected = { selected ->
                            repeats = selected.toString()
                        })
                }

                Box(modifier = Modifier.weight(1f)) {
                    StyledSelect(
                        options = listOf("1d", "1w", "1m", "2d", "2w", "2m"),
                        label = "Occurs",
                        initialSelected = occurs,
                        onOptionSelected = { selected ->
                            occurs = selected.toString()
                        })
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                StyledCircleLoader(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
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
                        StyledCheckbox(
                            checked = selectedUserIds.contains(user.id), onCheckedChange = {
                                selectedUserIds = if (it) {
                                    selectedUserIds + user.id
                                } else {
                                    selectedUserIds - user.id
                                }
                            })
                    }
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Text(errorMessage ?: "", color = Color.Red)
            }

            StyledButton(
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

                            val request = ApiCreateTaskRequest(
                                title = title,
                                date = "${deadline}T$dueTime",
                                description = description,
                                assignees = selectedUserIds.map {
                                    ApiAssignee(
                                        it.toString(), "todo"
                                    )
                                },
                                frequency = occurs,
                                repeat = repeats
                            )

                            val gson = Gson()
                            Log.d("TASK_JSON", gson.toJson(request))


                            val res = roomId?.let {
                                ApiClient.apiService.createTask(
                                    it, request = request, token = authToken
                                )
                            }
                            if (res != null) {
                                if (res.isSuccessful) {
                                    withContext(Dispatchers.Main) {
                                        onNavigateBack?.invoke()
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        errorMessage = "Failed to create task: ${res.code()}"
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = e.localizedMessage
                            }
                        }
                    }
                },
                text = "Create Task",
                buttonType = ButtonType.Primary,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}

