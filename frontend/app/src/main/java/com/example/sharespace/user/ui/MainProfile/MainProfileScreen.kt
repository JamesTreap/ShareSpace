package com.example.sharespace.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.core.data.local.TokenStorage
import com.example.sharespace.core.ui.theme.TextSecondary
import com.example.sharespace.user.ui.MainProfile.components.RoomCard
import com.example.sharespace.user.ui.MainProfile.components.RoomSectionHeader
import com.example.sharespace.user.ui.MainProfile.components.UserHeader
import com.example.sharespace.user.viewmodel.ProfileScreenViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Float): String {
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
}

@Composable
fun MainProfileScreen(
    viewModel: ProfileScreenViewModel = viewModel(factory = ProfileScreenViewModel.Factory),
    onCreateRoomClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToRoom: () -> Unit,
    onLogOut: () -> Unit,
    onViewProfileClick: () -> Unit,
) {

    val user by viewModel.user
    val rooms by viewModel.rooms.collectAsState()
    val invites by viewModel.invites.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
//    val tokenState = produceState<String?>(initialValue = null) {
//        value = TokenStorage.getToken(context)
//    }

    LaunchedEffect(Unit) {
        viewModel.onProfileScreenEntered()
    }

    viewModel.loadData()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
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


            rooms.forEach { room ->
                RoomCard(
                    room = room, showAction = false,
                    acceptInvite = {  },
                    declineInvite = {  },
                    room.alerts,
                    navigateToRoom = {
                        viewModel.setActiveRoom(room.id)
                        onNavigateToRoom()
                    })
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Pending Invites",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            invites.forEach { room ->
                RoomCard(
                    room = room, showAction = true,
                    acceptInvite = { viewModel.acceptInvite(room.id) },
                    declineInvite = { viewModel.declineInvite(room.id) },
                    room.alerts,
                    navigateToRoom = { }
                )
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