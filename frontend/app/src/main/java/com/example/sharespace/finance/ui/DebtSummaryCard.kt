// DebtSummaryCard.kt - New composable for displaying debt summaries
package com.example.sharespace.ui.screens.finance.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.data.repository.dto.users.ApiUserWithDebts
import com.example.sharespace.core.data.repository.dto.users.DebtSummary
import com.example.sharespace.core.ui.components.StyledButton
import com.example.sharespace.core.ui.theme.BackgroundAccent
import com.example.sharespace.core.ui.theme.BorderPrimary
import com.example.sharespace.core.ui.theme.ButtonRadius
import com.example.sharespace.core.ui.theme.TextPrimary
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtSummarySection(
    debtSummaries: List<DebtSummary>, onPayUser: (Int, String) -> Unit, // userId, userName
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BorderPrimary,
                shape = RoundedCornerShape(ButtonRadius)
            ),
        shape = RoundedCornerShape(ButtonRadius),
        colors = CardDefaults.cardColors(
            containerColor = BackgroundAccent
        )
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Debt Summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (debtSummaries.isEmpty()) {
                Text(
                    text = "All debts are settled! 🎉",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(debtSummaries) { summary ->
                        DebtSummaryItem(
                            summary = summary,
                            currencyFormatter = currencyFormatter,
                            onPayUser = onPayUser
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtSummaryItem(
    summary: DebtSummary, currencyFormatter: NumberFormat, onPayUser: (Int, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = if (summary.netBalance > 0) {
                Color(0xFF4CAF50).copy(alpha = 0.1f) // Light green - they owe you
            } else {
                Color(0xFFF44336).copy(alpha = 0.1f) // Light red - you owe them
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = summary.userName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                if (summary.netBalance > 0) {
                    Text(
                        text = "${summary.userName} owes you ${currencyFormatter.format(summary.netBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50)
                    )
                } else {
                    Text(
                        text = "You owe ${summary.userName} ${currencyFormatter.format(-summary.netBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336)
                    )
                }
            }

            if (summary.netBalance < 0) { // You owe them money
                StyledButton(
                    onClick = { onPayUser(summary.userId, summary.userName) },
                    text = "Pay"
                )
//                Button(
//                    onClick = { onPayUser(summary.userId, summary.userName) },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primary
//                    )
//                ) {
//                    Text("Pay")
//                }
            }
        }
    }
}

// DebtDetailsDialog.kt - Detailed debt breakdown dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtDetailsDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    roomMembersWithDebts: List<ApiUserWithDebts>,
    currentUserId: Int
) {
    if (!isVisible) return

    val currentUserData = roomMembersWithDebts.find { it.id == currentUserId }
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Detailed Debt Breakdown") }, text = {
        LazyColumn {
            if (currentUserData != null) {
                // What you owe others
                if (currentUserData.owes.isNotEmpty()) {
                    item {
                        Text(
                            "You owe:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(currentUserData.owes.entries.toList()) { (userIdStr, amount) ->
                        val otherUser = roomMembersWithDebts.find { it.id.toString() == userIdStr }
                        if (otherUser != null && amount > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(otherUser.name ?: otherUser.username)
                                Text(
                                    currencyFormatter.format(amount), color = Color(0xFFF44336)
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // What others owe you
                if (currentUserData.debts.isNotEmpty()) {
                    item {
                        Text(
                            "Others owe you:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(currentUserData.debts.entries.toList()) { (userIdStr, amount) ->
                        val otherUser = roomMembersWithDebts.find { it.id.toString() == userIdStr }
                        if (otherUser != null && amount > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(otherUser.name ?: otherUser.username)
                                Text(
                                    currencyFormatter.format(amount), color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }
        }
    }, confirmButton = {
        TextButton(onClick = onDismiss) {
            Text("Close")
        }
    })
}