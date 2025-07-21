package com.example.sharespace.user.ui.MainProfile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.theme.AquaAccent
import com.example.sharespace.core.ui.theme.TextSecondary

@Composable
fun RoomSectionHeader(
    title: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The title text on the left
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        // The clickable action text on the right
        Button(
            modifier = Modifier.height(30.dp),
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = AquaAccent
            ),
            shape = RoundedCornerShape(25),
            border = BorderStroke(1.dp, Color.LightGray),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = "+ Create Room",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}