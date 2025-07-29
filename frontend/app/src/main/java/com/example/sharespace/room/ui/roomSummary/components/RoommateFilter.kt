package com.example.sharespace.room.ui.roomSummary.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.domain.model.User


@Composable
fun RoommateFilter(
    roommates: List<User>, selectedRoommates: Set<Int>, onSelectionChanged: (Set<Int>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter by roommates: " + if (selectedRoommates.isEmpty()) "All"
                else "${selectedRoommates.size} selected",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Toggle filter"
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                roommates.forEach { user ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newSelection = selectedRoommates.toMutableSet()
                            if (user.id in selectedRoommates) {
                                newSelection.remove(user.id)
                            } else {
                                newSelection.add(user.id)
                            }
                            onSelectionChanged(newSelection)
                        }
                        .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = user.id in selectedRoommates, onCheckedChange = { checked ->
                                val newSelection = selectedRoommates.toMutableSet()
                                if (checked) newSelection.add(user.id) else newSelection.remove(user.id)
                                onSelectionChanged(newSelection)
                            })

                        Text(user.username)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onSelectionChanged(emptySet()) }) {
                        Text("Clear All")
                    }
                }
            }
        }
    }
}