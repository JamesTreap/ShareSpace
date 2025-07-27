package com.example.sharespace.room.ui.roomSummary.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.components.Avatar
import com.example.sharespace.room.viewmodel.RoomSummaryRoommatesUiState

@Composable
fun RoommatesSection(
    roommatesUiState: RoomSummaryRoommatesUiState, // Changed to accept UiState
    onAdd: () -> Unit, onViewAll: () -> Unit, onRetry: () -> Unit, // Add a retry callback
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = "Roommates", actionText = "View All", onAction = onViewAll)

        when (roommatesUiState) {
            is RoomSummaryRoommatesUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp) // Give a fixed height during loading
                        .padding(vertical = 8.dp), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is RoomSummaryRoommatesUiState.Success -> {
                val roommates = roommatesUiState.roommates
                if (roommates.isEmpty()) {
                    Row(
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
                                .clickable(onClick = onAdd),
                            shape = CircleShape
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add roommate")
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // "Add roommate" button as the first item for consistency
                        item {
                            OutlinedCard(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clickable(onClick = onAdd),
                                shape = CircleShape
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add roommate")
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp)) // Add space after the button
                        }
                        items(roommates) { user ->
                            Avatar(
                                photoUrl = user.photoUrl,
                                size = 56.dp, // Ensure Avatars have a defined size
                                contentDescription = "Avatar of ${user.name}"
                            )
                        }
                    }
                }
            }

            is RoomSummaryRoommatesUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp) // Give a fixed height for error state
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Failed to load roommates.", color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}