package com.example.sharespace.user.ui.MainProfile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.components.Avatar
import com.example.sharespace.core.ui.theme.TextSecondary

@Composable
fun UserHeader(name: String, photoUrl: String?, onViewProfileClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp, start = 18.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(photoUrl = photoUrl, contentDescription = "$name's avatar")

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Hi $name!", style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Easily split bills and track tasks",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3-dot menu
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert, contentDescription = "Menu"
                )
            }
            DropdownMenu(
                expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("View Profile") }, onClick = {
                    expanded = false
                    onViewProfileClick()
                })
            }
        }

    }
    HorizontalDivider(
        color = Color.LightGray.copy(alpha = 0.8f),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 18.dp)
    )
}
