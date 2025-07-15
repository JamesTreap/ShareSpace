package com.example.sharespace.core.ui.components
import BreakdownItem
import ListCard
import ScreenHeader
import SummaryCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp



@Composable
fun BillsListScreen(
    onNavigateBack: () -> Unit = {},
    onAddBill: () -> Unit = {},
    onBillClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        item {
            ScreenHeader(
                title = "Bill Overview",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = onAddBill) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Bill"
                        )
                    }
                }
            )
        }

        // Summary Card
        item {
            SummaryCard(
                totalAmount = "$993.12",
                period = "Last 30d",
                breakdownItems = listOf(
                    BreakdownItem("Rent", "$835.00"),
                    BreakdownItem("Utilities", "$232.34"),
                    BreakdownItem("Entertainment", "$734.81"),
                    BreakdownItem("Unknown", "$634.45")
                )
            )
        }

        // Roommates Section
        item {
            SectionHeader(
                title = "Roommates (3)",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Roommate Cards
        items(getRoommateData()) { roommate ->
            ListCard(
                title = roommate.name,
                subtitle = "Amount owed: ${roommate.amount}",
                leadingIcon = {
                    Avatar(roommate.name)
                },
                trailingIcon = {
                    DollarIcon()
                },
                onClick = { onBillClick(roommate.id) }
            )
        }

        // Transaction History Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History (734)",
                    style = MaterialTheme.typography.headlineSmall
                )
                OutlinedButton(onClick = onAddBill) {
                    Text("+ Add Bill")
                }
            }
        }

        // Transaction Items
        items(getTransactionData()) { transaction ->
            ListCard(
                title = transaction.title,
                subtitle = "${transaction.date} | ${transaction.amount}",
                trailingIcon = {
                    IconButton(onClick = { /* Delete */ }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            )
        }

        // View More Button
        item {
            Button(
                onClick = { /* View more */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("View More")
            }
        }
    }
}

// Helper composables
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
fun Avatar(name: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DollarIcon() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

// Data classes and mock data
data class Roommate(
    val id: String,
    val name: String,
    val amount: String
)

data class Transaction(
    val id: String,
    val title: String,
    val date: String,
    val amount: String
)

fun getRoommateData() = listOf(
    Roommate("1", "Roommate 1", "$634"),
    Roommate("2", "Roommate 2", "0"),
    Roommate("3", "Roommate 3", "$844")
)

fun getTransactionData() = listOf(
    Transaction("1", "Roommate #1 Sent Money", "June 20", "$100"),
    Transaction("2", "Disneyland", "June 16", "$6434.53"),
    Transaction("3", "Electricity", "June 12", "$65.34"),
    Transaction("4", "Backyard Cannon", "June 6", "$893.84")
)