package com.example.sharespace.room.ui.roomSummary.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.domain.model.User
import com.example.sharespace.core.ui.components.Avatar

@Composable
fun RoommatesSection(
    roommates: List<User>, onAdd: () -> Unit, onViewAll: () -> Unit
) {
    // This Column is a fixed part of the main screen layout
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = "Roommates", actionText = "View All", onAction = onViewAll)
        if (roommates.isEmpty()) {
            Row( // Using Row to align text and add button
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("No roommates yet. Add one!", modifier = Modifier.weight(1f))
                OutlinedCard(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(onClick = onAdd), shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Add, contentDescription = "Add roommate")
                    }
                }
            }
            return@Column
        }
        // LazyRow provides horizontal scrolling for roommates
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            item { // Add roommate button at the end
                OutlinedCard(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(onClick = onAdd), shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Add, contentDescription = "Add roommate")
                    }
                }
            }
            items(roommates) { user ->
                Avatar(
                    photoUrl = user.photoUrl, contentDescription = "Avatar of ${user.name}"
                )
            }
        }
    }
}