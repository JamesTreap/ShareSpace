package com.example.sharespace.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sharespace.core.ui.components.SectionHeader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import com.example.sharespace.core.data.local.TokenStorage
import com.example.sharespace.user.data.repository.ProfileRepository
import kotlinx.coroutines.launch
import com.example.sharespace.core.ui.theme.Typography


data class User(
    val id: String,
    val name: String,
    val photoUrl: String? = null
)


data class Room (
    val id: String,
    val name: String,
    val members: Int,
    val due: Float,
    val notifications: Int = 0,
    val photoUrl: String? = null,
)


class ProfileScreenViewModel : ViewModel() {
    // backing state
    private val _user = mutableStateOf<User?>(null)
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    private val _invites = MutableStateFlow<List<Room>>(emptyList())

    // public streams
    val user: MutableState<User?> = _user
    val rooms: StateFlow<List<Room>> = _rooms
    val invites: MutableStateFlow<List<Room>> = _invites

    init {
//        loadSampleData()
    }

    fun acceptInvite() {

    }

    fun declineInvite() {

    }

    private val repository = ProfileRepository()

    fun loadData(token: String) {
//        println("Loading data with token: $token")
//        println("hfudsihujksalhfshfjlsdhk")

        viewModelScope.launch {
            try {
                val apiUser = repository.getUser(token)
//                println("Got user: ${apiUser.name}")

                val (joinedRooms, roomInvites) = repository.getRoomsAndInvites(token)
//                println("Got ${joinedRooms.size} joined rooms")
//                println("Got ${roomInvites.size} invites")

                _user.value = User(
                    id = apiUser.id.toString(),
                    name = apiUser.name,
                    photoUrl = apiUser.profilePictureUrl
                )

                _rooms.value = joinedRooms.map { room ->
                    Room(
                        id = room.id.toString(),
                        name = room.name,
                        members = room.members.size,
                        due = room.balanceDue,
                        notifications = room.alerts,
                        photoUrl = room.pictureUrl
                    )
                }

                _invites.value = roomInvites.map { invite ->
                    Room(
                        id = invite.roomId.toString(),
                        name = "Room ${invite.roomId}",
                        members = 0, // if you need details, another API call is needed
                        due = 0f,
                        notifications = 0,
                        photoUrl = null
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println("Error loading profile data: ${e.message}")
            }
        }
    }

    private fun loadSampleData() {
        _user.value = User(id = "u1", name = "Bob", photoUrl = "https://static.wikia.nocookie.net/naruto/images/2/21/Sasuke_Part_1.png/revision/latest/scale-to-width-down/1200?cb=20170716092103")

        _rooms.value = listOf(
            Room(id = "r1",
                name = "200 University Ave W.",
                members = 4,
                due = 2163f,
                photoUrl = "https://.../goose.jpg",
                notifications = 10),
            Room(id = "r2",
                name = "3828 Piermont Dr",
                members = 3,
                due = 0f,
                photoUrl = "https://.../walter.jpg",
                notifications = 20)
        )

        _invites.value = listOf(
            Room(id = "i1",
                name = "201 University Ave W.",
                members = 662,
                due = 314159f,
                photoUrl = "https://.../babygoose.jpg")
        )
    }
}

@Composable
fun EditProfileScreen(
    viewModel: ProfileScreenViewModel = viewModel(),
    onCreateRoomClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToRoom: () -> Unit,
    onLogOut: () -> Unit,
) {
    val user by viewModel.user
    val rooms by viewModel.rooms.collectAsState()
    val invites by viewModel.invites.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tokenState = produceState<String?>(initialValue = null) {
        value = TokenStorage.getToken(context)
    }
    val token = tokenState.value

    LaunchedEffect(token) {
        if (token != null) {
            println("Calling loadData with token: $token")
            viewModel.loadData(token)
        } else {
            println("Token is null, not calling loadData")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(vertical = 24.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            user?.let { UserHeader(name = it.name, photoUrl = it.photoUrl) }
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                title = "Your Rooms",
                actionText = "+ Create Room",
                onAction = onCreateRoomClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))


            rooms.forEach { room ->
                RoomCard(room = room, showAction = false,
                    acceptInvite = { viewModel.acceptInvite() },
                    declineInvite = { viewModel.declineInvite() },
                    room.notifications,
                    navigateToRoom = { onNavigateToRoom() })
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Pending Invites",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))

            Spacer(modifier = Modifier.height(8.dp))

            invites.forEach { room ->
                RoomCard(room = room, showAction = true,
                    acceptInvite = { viewModel.acceptInvite() },
                    declineInvite = { viewModel.declineInvite() },
                    room.notifications,
                    navigateToRoom = onNavigateToRoom)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        TokenStorage.clearToken(context)
                        onLogOut()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB00020), // Red background
                    contentColor = Color.White          // White text
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Log Out",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Out",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

    }

}

@Composable
fun UserHeader(name: String, photoUrl: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(photoUrl = photoUrl, contentDescription = "$name's avatar")

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Hi $name!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Easily split bills and track tasks",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

    }
    HorizontalDivider(color = Color.Black.copy(alpha = 0.8f), thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 18.dp))
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


@Composable
fun RoomCard(
    room: Room,
    showAction: Boolean,
    acceptInvite: () -> Unit,
    declineInvite: () -> Unit,
    numOfNotifications: Int,
    navigateToRoom: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        border = BorderStroke(1.dp, Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = navigateToRoom
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(photoUrl = room.photoUrl, size = 48.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${room.members} members | $${room.due} due",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (showAction) {
                Row { // Use Row for side-by-side buttons
                    Button(
                        onClick = acceptInvite,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Green
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = declineInvite,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)) // Red
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Decline",
                            tint = Color.White
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(6.dp))
                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(6.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = numOfNotifications.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
        }
    }
}