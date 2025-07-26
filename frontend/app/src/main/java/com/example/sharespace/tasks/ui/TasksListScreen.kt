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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.sharespace.ShareSpaceApplication
import com.example.sharespace.core.data.remote.ApiClient
import com.example.sharespace.core.data.repository.dto.ApiTask
import com.example.sharespace.core.ui.components.NavigationHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import com.example.sharespace.core.ui.components.StyledButton
import com.example.sharespace.core.ui.components.ButtonType
import com.example.sharespace.core.ui.components.StyledCircleLoader
import com.example.sharespace.core.ui.components.StyledLineLoader
import com.example.sharespace.core.ui.theme.AquaAccent
import com.example.sharespace.core.ui.components.Avatar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    onNavigateBack: () -> Unit,
    onAddTaskClick: () -> Unit = {},
    onEditTaskClick: (Int) -> Unit
)
 {
     val context = LocalContext.current
     val userSessionRepository = (context.applicationContext as ShareSpaceApplication).container.userSessionRepository
     val roomId by userSessionRepository.activeRoomIdFlow.collectAsState(initial = null)
     val authToken by userSessionRepository.userTokenFlow.collectAsState(initial = null)
     var taskSummary by remember { mutableStateOf(listOf<String>()) }
     var allTasks by remember { mutableStateOf(listOf<ApiTask>()) }
     var inProgressTasks by remember { mutableStateOf<List<TaskData>>(emptyList()) }
     var completedTasks by remember { mutableStateOf<List<TaskData>>(emptyList()) }
     var error by remember { mutableStateOf<String?>(null) }
     var isLoading by remember { mutableStateOf(true) }
     var profilePicMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

     if (roomId == null) {
         StyledCircleLoader()
         return
     }
    // Fetch task data from API

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.e("token is ", "Fis is is  $authToken")
                // ← Replace with real token or maybe just ignore it
                //Log.d("ID", "Fetched ROOMID: $roomId")
                val response = roomId?.let { authToken?.let { it1 ->
                    ApiClient.apiService.getTasksForRoom(it,
                        it1
                    )
                } }
//                if (response != null) {
//                    Log.d("TASKS", "Fetched tasks: ${response.body()}")
//                }

                if (response != null) {
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
                        val userIdToProfilePicUrl = mutableMapOf<Int, String>()

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
                                val userResp =
                                    authToken?.let {
                                        ApiClient.apiService.getUserDetailsById(id,
                                            it
                                        )
                                    }
                                if (userResp != null) {
                                    if (userResp.isSuccessful) {
                                        userResp.body()?.let {
                                            userIdToName[id.toString()] = it.name
                                            it.profilePictureUrl?.let { url ->
                                                userIdToProfilePicUrl[id] = url
                                                Log.d("PROFILE_PIC_MAP", "id=$id, url=$url")
                                            }
                                        }
                                    }
                                }
                            } catch (_: Exception) {
                            }
                        }

                        profilePicMap = userIdToProfilePicUrl.toMap()


                        // Now that names are available, convert summary
                        val generatedSummary = userTaskMap.entries.map { (userId, pair) ->
                            val name = userIdToName[userId] ?: "User $userId"
                            "$name - ${pair.first}/${pair.second} Complete"
                        }



                        for (id in userIds) {
                            try {
                                val userResp =
                                    authToken?.let {
                                        ApiClient.apiService.getUserDetailsById(id,
                                            it
                                        )
                                    }
                                if (userResp != null) {
                                    if (userResp.isSuccessful) {
                                        userResp.body()?.let { userIdToName[id.toString()] = it.name }
                                    }
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

                            val assigneeNames = statuses.keys.map { uid ->
                                userIdToName[uid] ?: "User $uid"
                            }

                            val assigneeIds = statuses.keys.mapNotNull { it.toIntOrNull() }

                            TaskData(
                                id = task.id,
                                title = task.title,
                                status = finalStatus,
                                assignees = assigneeNames,
                                assigneeIds = assigneeIds // ✅ add this
                            )
                        }

                        inProgressTasks = mapped.filter { it.status != "COMPLETE" }
                        completedTasks = mapped.filter { it.status == "COMPLETE" }
                        taskSummary = generatedSummary
                        isLoading = false
                    } else {
                        error = "API Error: ${response.code()}"
                    }
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

     fun updateTaskLists(apiTasks: List<ApiTask>) {
         val today = LocalDate.now()
         val thirtyDaysAgo = today.minusDays(30)

         val filteredTasks = apiTasks.filter { task ->
             try {
                 val deadline = LocalDate.parse(task.deadline.substringBefore("T"))
                 !deadline.isAfter(today) && !deadline.isBefore(thirtyDaysAgo)
             } catch (e: Exception) {
                 false
             }
         }

         allTasks = filteredTasks

         // Recalculate status summaries
         val userTaskMap = mutableMapOf<String, Pair<Int, Int>>()
         for (task in filteredTasks) {
             for ((userId, status) in task.statuses) {
                 val (completed, total) = userTaskMap[userId] ?: (0 to 0)
                 val newCompleted = if (status == "COMPLETE") completed + 1 else completed
                 userTaskMap[userId] = (newCompleted to total + 1)
             }
         }

         val generatedSummary = userTaskMap.entries.map { (userId, pair) ->
             val name = userId // If you want to show userId or retrieve real name, modify this
             "$name - ${pair.first}/${pair.second} Complete"
         }
         taskSummary = generatedSummary

         // Recompute UI list
         val mapped = filteredTasks.map { task ->
             val statusValues = task.statuses.values.toSet()

             val finalStatus = when {
                 statusValues.isEmpty() -> "ASSIGNED"
                 statusValues.all { it == "COMPLETE" } -> "COMPLETE"
                 statusValues.any { it == "IN-PROGRESS" } -> "IN-PROGRESS"
                 else -> "ASSIGNED"
             }

             val assigneeNames = task.statuses.keys.map { it }
             val assigneeIds = task.statuses.keys.mapNotNull { it.toIntOrNull() }

             TaskData(
                 id = task.id,
                 title = task.title,
                 status = finalStatus,
                 assignees = assigneeNames,
                 assigneeIds = assigneeIds
             )
         }

         inProgressTasks = mapped.filter { it.status != "COMPLETE" }
         completedTasks = mapped.filter { it.status == "COMPLETE" }
     }


     Scaffold(
        topBar = {
            NavigationHeader(
                title = "Task Overview",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Task Summary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    StyledCircleLoader(modifier = Modifier
                        .size(80.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.Transparent, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.matchParentSize()
                        ) {
                            drawCircle(
                                color = AquaAccent,
                                radius = size.minDimension / 2,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${allTasks.size} Tasks",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text("Last 30d", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isLoading) {
                        repeat(5) {
                            StyledLineLoader(modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        taskSummary.forEach {
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                lineHeight = 14.sp
                            )
                        }
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
                StyledButton(
                    onClick = { onAddTaskClick() },
                    text = "+Add Task",
                    buttonType = ButtonType.Tertiary
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                StyledCircleLoader(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(inProgressTasks) { task ->
                        TaskCard(task = task, onEditClick = onEditTaskClick)
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            // Completed Tasks
            Text("Completed Tasks (${completedTasks.size})", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                StyledCircleLoader(modifier = Modifier.align(Alignment.CenterHorizontally))
                return@Column // skip rendering the rest while loading
            }

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
                        CompletedTaskRow(
                            task = task,
                            profilePicMap = profilePicMap,
                            onEditTaskClick = { onEditTaskClick(it) },
                            onDeleteTaskClick = { taskId ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        Log.e("token is ", "is is is  $authToken")
                                        val token = "Bearer ${authToken ?: ""}"
                                        val response = ApiClient.apiService.deleteTask(taskId, token)
                                        if (response.isSuccessful) {
                                            // After successful deletion, reload tasks
                                            val updatedTasks = roomId?.let { ApiClient.apiService.getTasksForRoom(it, token) }
                                            if (updatedTasks != null && updatedTasks.isSuccessful) {
                                                val apiTasks = updatedTasks.body() ?: emptyList()
                                                updateTaskLists(apiTasks)
                                                isLoading = false
                                            }
                                        } else {
                                            Log.e("DELETE_TASK", "Failed to delete task: ${response.code()}")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("DELETE_TASK", "Error during deletion: ${e.message}")
                                    }
                                }
                            }

                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Column {
                    displayedTasks.forEach { task ->
                        CompletedTaskRow(
                            task = task,
                            profilePicMap = profilePicMap,
                            onEditTaskClick = { onEditTaskClick(it) },
                            onDeleteTaskClick = { taskId ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        Log.e("token is ", "is is is  $authToken")
                                        val token = "Bearer ${authToken ?: ""}"
                                        val response = ApiClient.apiService.deleteTask(taskId, token)
                                        if (response.isSuccessful) {
                                            // After successful deletion, reload tasks
                                            val updatedTasks = roomId?.let { ApiClient.apiService.getTasksForRoom(it, token) }
                                            if (updatedTasks != null && updatedTasks.isSuccessful) {
                                                val apiTasks = updatedTasks.body() ?: emptyList()
                                                updateTaskLists(apiTasks)
                                                isLoading = false
                                            }
                                        } else {
                                            Log.e("DELETE_TASK", "Failed to delete task: ${response.code()}")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("DELETE_TASK", "Error during deletion: ${e.message}")
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (hasMoreTasks) {
                StyledButton(
                    onClick = { showAllCompleted = !showAllCompleted },
                    text = if (showAllCompleted) "View Less" else "View More",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    buttonType = ButtonType.Primary
                )

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
            StyledButton(
                onClick = { onEditClick(task.id) },
                text = "Edit Task",
                modifier = Modifier.fillMaxWidth(),
                buttonType = ButtonType.Primary
            )

        }
    }
}


@Composable
fun CompletedTaskRow(
    task: TaskData,
    profilePicMap: Map<Int, String>,
    onEditTaskClick: (Int) -> Unit,
    onDeleteTaskClick: (Int) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Overlapping Avatars
        Row(
            modifier = Modifier.height(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val maxAvatars = 2
            val displayedMembers = task.assigneeIds.take(maxAvatars)

            displayedMembers.forEachIndexed { index, userId ->
                val photoKey = profilePicMap[userId]
                Box(
                    modifier = Modifier
                        .zIndex((maxAvatars - index).toFloat())
                        .offset(x = (-8 * index).dp)
                ) {
                    Avatar(
                        photoUrl = photoKey,
                        contentDescription = "Avatar for user $userId",
                        size = 36.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(task.title, fontWeight = FontWeight.Bold)
            Text(task.assignees.joinToString(", "), fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Three-dot menu
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        menuExpanded = false
                        onEditTaskClick(task.id)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        menuExpanded = false
                        onDeleteTaskClick(task.id)
                    }
                )
            }
        }
    }
}

data class TaskData(
    val id: Int,
    val title: String,
    val status: String,
    val assignees: List<String>,
    val assigneeIds: List<Int>
)


