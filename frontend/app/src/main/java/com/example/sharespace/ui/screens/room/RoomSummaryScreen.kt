@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sharespace.ui.screens.room


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Bill(
    val id: String, val title: String, val amount: Double, val subtitle: String
)

data class User(
    val id: String, val name: String, val photoUrl: String? = null
)

data class Task(
    val id: String,
    val title: String,
    val dueDate: LocalDateTime,
    val iconResId: Int? = null,
    val isDone: Boolean = false
)

@RequiresApi(Build.VERSION_CODES.O)
class RoomSummaryViewModel : ViewModel() {
    // backing state
    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    private val _roommates = MutableStateFlow<List<User>>(emptyList())
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    // public streams
    val bills: StateFlow<List<Bill>> = _bills
    val roommates: StateFlow<List<User>> = _roommates
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        loadSampleData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadSampleData() {
        _bills.value = listOf(
            Bill(id = "b1", title = "Rent", amount = 100.0, subtitle = "Owing to Roommate 1"),
            Bill(id = "b2", title = "Hydro", amount = 50.0, subtitle = "Owing to Roommate 2"),
            Bill(id = "b3", title = "Cookware", amount = 25.0, subtitle = "Your share")
        )

        _roommates.value = listOf(
            User(id = "u1", name = "Alice", photoUrl = "https://.../alice.jpg"),
            User(id = "u2", name = "Bob", photoUrl = "https://.../bob.jpg"),
            User(id = "u3", name = "Carol", photoUrl = null)
        )

        _tasks.value = listOf(
            Task(
                id = "t1",
                title = "Take out garbage",
                dueDate = LocalDateTime.of(2025, 6, 10, 13, 50),
                isDone = false
            ), Task(
                id = "t2",
                title = "Do recycling",
                dueDate = LocalDateTime.of(2025, 6, 11, 14, 50),
                isDone = false
            ), Task(
                id = "t3",
                title = "Summon Cthulhu",
                dueDate = LocalDateTime.of(2025, 6, 19, 6, 6),
                isDone = false
            )
        )
    }

    fun payBill(bill: Bill) {
        // TODO: mark as paid / update your backend
    }

    fun addRoommate(user: User) {
        _roommates.value = _roommates.value + user
    }

    fun toggleTaskDone(task: Task) {
        _tasks.value = _tasks.value.map {
            if (it.id == task.id) it.copy(isDone = !it.isDone) else it
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoomSummaryScreen(
    viewModel: RoomSummaryViewModel = viewModel(),
    onViewBillsClick: () -> Unit,
    onAddRoommateClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onViewTasksClick: () -> Unit,
    onFinanceManagerClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val bills by viewModel.bills.collectAsState()
    val roommates by viewModel.roommates.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    Scaffold(
        topBar = {
            RoomSummaryTopAppBar(
                address = "200 University Ave W.",
                subtitle = "My amazing desc here",
                onNavigateBack= onNavigateBack
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Button(
                onClick = onFinanceManagerClick,
                modifier =
                    Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Text(text = "Finance Manager")
            }
            RecentBillsSection(bills = bills, onPay = { /*…*/ }, onViewAll = onViewBillsClick)
            RoommatesSection(
                roommates = roommates,
                onAdd = onAddRoommateClick,
                onViewAll = { /*…*/ })
            UpcomingTasksSection(
                tasks = tasks,
                onToggleDone = { /*…*/ },
                onAdd = onAddTaskClick,
                onViewAll = onViewTasksClick
            )
//            CalendarSection( /*…*/ )
//            Button(
//                onClick = onViewBillsClick,
//                modifier = Modifier.widthIn(min = 250.dp)
//            ) {
//                Text(text = "View all bills")
//            }
//            Button(
//                onClick = onAddRoommateClick,
//                modifier = Modifier.widthIn(min = 250.dp)
//            ) {
//                Text(text = "Add roommate")
//            }
//            Button(
//                onClick = onAddTaskClick,
//                modifier = Modifier.widthIn(min = 250.dp)
//            ) {
//                Text(text = "Add tasks")
//            }
//            Button(
//                onClick = onViewTasksClick,
//                modifier = Modifier.widthIn(min = 250.dp)
//            ) {
//                Text(text = "View all tasks")
//            }
        }
    }
}


@Composable
fun RoomSummaryTopAppBar(address: String, subtitle: String,  onNavigateBack: () -> Unit ) {
    TopAppBar(title = {
        Column {
            Text(text = address, style = MaterialTheme.typography.titleLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }, navigationIcon = {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    })
}

@Composable
fun RecentBillsSection(
    bills: List<Bill>, onPay: (Bill) -> Unit, onViewAll: () -> Unit
) {
    SectionHeader(title = "Recent Bills", actionText = "View All", onAction = onViewAll)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(bills) { bill ->
            ElevatedCard(modifier = Modifier.width(180.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(bill.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("$${bill.amount}", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(bill.subtitle, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onPay(bill) }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Pay User")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String, actionText: String? = null, actionIcon: ImageVector? = null, onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        when {
            actionText != null -> TextButton(onClick = onAction) { Text(actionText) }

            actionIcon != null -> IconButton(onClick = onAction) {
                Icon(
                    actionIcon, contentDescription = null
                )
            }
        }
    }
}

@Composable
fun RoommatesSection(
    roommates: List<User>, onAdd: () -> Unit, onViewAll: () -> Unit
) {
    SectionHeader(title = "Roommates", actionText = "View All", onAction = onViewAll)

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(roommates) { user ->
            Avatar(
                photoUrl = user.photoUrl, contentDescription = "Avatar of ${user.name}"
            )
        }
        item {
            OutlinedCard(
                modifier = Modifier
                    .size(56.dp)
                    .clickable(onClick = onAdd),
                shape = CircleShape,
                colors = CardDefaults.outlinedCardColors()
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add roommate",
                    Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun Avatar(
    photoUrl: String?, contentDescription: String? = null, size: Dp = 56.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.surfaceVariant) // subtle backdrop
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = contentDescription,
                modifier = Modifier
                    .matchParentSize()
                    .padding(12.dp)
            )
        }
    }
}

//@Composable
//fun UpcomingTasksSection(
//    tasks: List<Task>, onAdd: () -> Unit, onToggleDone: (Task) -> Unit
//) {
//    SectionHeader(
//        title = "Upcoming Tasks", actionIcon = Icons.Default.Add, onAction = onAdd
//    )
//
//    LazyColumn(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        items(tasks) { task ->
//            ListItem(
//                // optional small text above the headline
//                overlineContent = { /* e.g. weekday or category if you want */ },
//
//                // main title
//                headlineContent = {
//                    Text(
//                        text = task.title, style = MaterialTheme.typography.titleMedium
//                    )
//                },
//
//                // subtitle / supporting text
//                supportingContent = {
//                    Text(
//                        text = task.dueDate.format(DateTimeFormatter.ofPattern("MMM d | h:mm a")),
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                },
//
//                // optional icon or avatar on the left
//                leadingContent = {
//                    Avatar(
//                        photoUrl = "", size = 40.dp, contentDescription = null
//                    )
//                },
//
//                // check‐off button on the right
//                trailingContent = {
//                    IconButton(onClick = { onToggleDone(task) }) {
//                        Icon(
//                            imageVector = if (task.isDone) Icons.Filled.CheckCircle
//                            else Icons.Outlined.CheckCircle,
//                            contentDescription = if (task.isDone) "Completed" else "Mark done"
//                        )
//                    }
//                },
//
//                // keep default colors/elevation
//                colors = ListItemDefaults.colors(),
//                tonalElevation = ListItemDefaults.Elevation,
//                shadowElevation = ListItemDefaults.Elevation,
//            )
//
//            Divider()
//        }
//    }
//}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpcomingTasksSection(
    tasks: List<Task>,
    onAdd: () -> Unit,
    onToggleDone: (Task) -> Unit,
    onViewAll: () -> Unit          // ← new callback
) {
    SectionHeader(
        title = "Upcoming Tasks",
        actionText = "+ Add Task",
        onAction = onAdd
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(tasks) { task ->
            ListItem(
                headlineContent = { Text(task.title) },
                supportingContent = {
                    Text(task.dueDate.format(DateTimeFormatter.ofPattern("MMM d | h:mm a")))
                },
                leadingContent = {
                    Avatar(photoUrl = "", size = 40.dp)
                },
                trailingContent = {
                    IconButton(onClick = { onToggleDone(task) }) {
                        Icon(
                            imageVector = if (task.isDone) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = null
                        )
                    }
                }
            )
            HorizontalDivider()
        }

        // ← here’s the missing “View all” button
        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onViewAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Text("View all")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
