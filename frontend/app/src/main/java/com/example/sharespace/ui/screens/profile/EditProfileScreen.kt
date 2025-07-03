package com.example.sharespace.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import com.example.sharespace.ui.screens.room.SectionHeader
import androidx.compose.foundation.shape.RoundedCornerShape



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
        loadSampleData()
    }

    fun acceptInvite() {

    }

    fun declineInvite() {

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
) {
    val user by viewModel.user
    val rooms by viewModel.rooms.collectAsState()
    val invites by viewModel.invites.collectAsState()
    val scrollState = rememberScrollState()
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize().verticalScroll(scrollState)
        ) {
            user?.let { UserHeader(name = it.name, photoUrl = user?.photoUrl) }
            Spacer(modifier = Modifier.height(16.dp))

            Box (modifier = Modifier.padding(horizontal = 4.dp)) {
                SectionHeader(
                    title = "Your Rooms",
                    actionText = "+ Create Room",
                    onAction = onCreateRoomClick
                )
            }

            rooms.forEach { room ->
                RoomCard(room = room, showAction = false,
                    acceptInvite = { viewModel.acceptInvite() },
                    declineInvite = { viewModel.declineInvite() },
                    room.notifications)
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
                    room.notifications)
                Spacer(modifier = Modifier.height(8.dp))
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
    numOfNotifications: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        border = BorderStroke(1.dp, Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                Column {
                    Button(
                        onClick = acceptInvite,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B5998)) // deep blue
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.White)
                    }

                    Button(
                        onClick = declineInvite,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)) // red
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Decline", tint = Color.White)
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