package com.example.sharespace.user.ui.MainProfile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.theme.AlertRed
import com.example.sharespace.core.ui.theme.AquaAccent
import com.example.sharespace.core.ui.theme.TextSecondary
import com.example.sharespace.ui.screens.profile.Room
import com.example.sharespace.ui.screens.profile.formatCurrency

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
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = navigateToRoom
    ) {
        Row(
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                )
                val memberLabel = if (room.members == 1) "member" else "members"
                val dueColor = if (room.due > 0f) AlertRed else TextSecondary
                Text(
                    text = "${room.members} $memberLabel | ${formatCurrency(room.due)} due",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dueColor
                )
            }

            if (showAction) {
                Row { // Use Row for side-by-side buttons
                    Button(
                        onClick = acceptInvite,
                        shape = RoundedCornerShape(30),
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AquaAccent)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = declineInvite,
                        shape = RoundedCornerShape(30),
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed) // Red
                    ) {
                        Icon(
                            Icons.Default.Close, contentDescription = "Decline", tint = Color.White
                        )
                    }
                }
            } else {
                val badgeColor = if (numOfNotifications > 0) AlertRed else Color(0xFFE0E0E0)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(badgeColor, shape = RoundedCornerShape(100))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = numOfNotifications.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                    )
                }

            }
        }
    }
}