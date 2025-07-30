package com.example.sharespace.room.ui.roomSummary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.components.AvatarSquare
import com.example.sharespace.core.ui.theme.BorderPrimary
import com.example.sharespace.core.ui.theme.ButtonRadius
import com.example.sharespace.room.viewmodel.RoomSummaryRoommatesUiState

@Composable
fun RoommatesSection(
    roommatesUiState: RoomSummaryRoommatesUiState,
    onAdd: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            title = "Roommates",
//            actionText = "View All",
            onAction = { }, modifier = Modifier.fillMaxWidth()
        )

        when (roommatesUiState) {
            is RoomSummaryRoommatesUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
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
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("No roommates yet. Add one!", modifier = Modifier.weight(1f))
                        OutlinedCard(
                            modifier = Modifier
                                .size(56.dp)
                                .clickable(onClick = onAdd),
                            shape = RoundedCornerShape(8.dp)
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
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
//                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = BorderPrimary,
                                shape = RoundedCornerShape(ButtonRadius)
                            )
                    ) {
                        item {
                            OutlinedCard(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clickable(onClick = onAdd),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add roommate")
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        items(roommates) { user ->
                            AvatarSquare(
                                photoUrl = user.photoUrl, size = 56.dp, cornerRadius = 8.dp
                            )
                        }
                    }
                }
            }

            is RoomSummaryRoommatesUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
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