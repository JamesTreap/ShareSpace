package com.example.sharespace.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.core.data.local.TokenStorage
import com.example.sharespace.core.ui.theme.TextSecondary
import com.example.sharespace.user.ui.MainProfile.components.RoomCard
import com.example.sharespace.user.ui.MainProfile.components.RoomSectionHeader
import com.example.sharespace.user.ui.MainProfile.components.UserHeader
import com.example.sharespace.user.viewmodel.InvitesUiState
import com.example.sharespace.user.viewmodel.ProfileScreenViewModel
import com.example.sharespace.user.viewmodel.RoomsUiState
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// This utility function is fine, no changes needed.
fun formatCurrency(amount: Float): String {
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
}

@Composable
fun MainProfileScreen(
    viewModel: ProfileScreenViewModel = viewModel(factory = ProfileScreenViewModel.Factory),
    onCreateRoomClick: () -> Unit,
    onNavigateBack: () -> Unit, // This parameter is unused. Consider using it in a TopAppBar.
    onNavigateToRoom: () -> Unit,
    onLogOut: () -> Unit,
    onViewProfileClick: () -> Unit,
    onFinanceManagerClick: () -> Unit, // This parameter is unused.
) {

    val user by viewModel.user
    val roomsUiState by viewModel.roomsUiState.collectAsState()
    val invitesUiState by viewModel.invitesUiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.onProfileScreenEntered()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp) // Moved padding here to apply to the content
        ) {
            user?.let { UserHeader(name = it.name, photoUrl = it.photoUrl, onViewProfileClick) }
            Spacer(modifier = Modifier.height(16.dp))

            RoomSectionHeader(
                title = "Your Rooms",
                actionText = "+ Create Room",
                onAction = onCreateRoomClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (roomsUiState) {
                is RoomsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }
                is RoomsUiState.Success -> {
                    val rooms = (roomsUiState as RoomsUiState.Success).rooms
                    if (rooms.isEmpty()) {
                        Text(
                            text = "You are not in any rooms yet.",
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        rooms.forEach { room ->
                            RoomCard(
                                room = room,
                                showAction = false,
                                acceptInvite = { },
                                declineInvite = { },
                                numOfNotifications = room.alerts,
                                navigateToRoom = {
                                    viewModel.setActiveRoom(room.id)
                                    onNavigateToRoom()
                                })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                is RoomsUiState.Error -> {
                    Text(
                        text = "Failed to load rooms. Please try again.",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Pending Invites",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (invitesUiState) {
                is InvitesUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }
                is InvitesUiState.Success -> {
                    val invites = (invitesUiState as InvitesUiState.Success).invites
                    if (invites.isEmpty()) {
                        Text(
                            text = "You have no pending invites.",
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        invites.forEach { room ->
                            RoomCard(
                                room = room,
                                showAction = true,
                                acceptInvite = { viewModel.acceptInvite(room.id) },
                                declineInvite = { viewModel.declineInvite(room.id) },
                                // FIXED: Changed `alerts` to `numOfNotifications` and passed the size.
                                numOfNotifications = room.alerts,
                                navigateToRoom = { }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                is InvitesUiState.Error -> {
                    Text(
                        text = "Failed to load invites. Please try again.",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        TokenStorage.clearToken(context)
                        onLogOut()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Log Out")
            }
        }
    }
}
