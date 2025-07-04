package com.example.sharespace.ui.screens.finance
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.sharespace.ui.components.NavigationHeader
import com.example.sharespace.ui.screens.room.SectionHeader

@Composable
fun FinanceManagerScreen(
    onNavigateBack: () -> Unit,
    onAddBillClick: () -> Unit
) {
    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Bill Overview",
                onNavigateBack = onNavigateBack
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface) // Use theme surface color
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            // Bill summary circle and breakdown
            item {
                BillSummarySection()
            }

            // Roommates section
            item {
                RoommatesSection()
            }

            // Transaction history
            item {
                TransactionHistorySection(onAddBillClick)
            }

            // View More button
            item {
                ViewMoreButton()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@Composable
fun BillSummarySection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular amount display
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant, // Use theme color
                    CircleShape
                )
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Use theme color
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$993.12",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
                )
                Text(
                    text = "Last 30d",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // Use theme color
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Bill breakdown
        Column {
            BillBreakdownItem("Rent", "$835.00")
            BillBreakdownItem("Utilities", "$232.34")
            BillBreakdownItem("Entertainment", "$734.81")
            BillBreakdownItem("Unknown", "$634.45")
        }
    }
}

@Composable
fun BillBreakdownItem(category: String, amount: String) {
    Text(
        text = "$category - $amount",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface, // Use theme color
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
fun RoommatesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Roommates (3)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        RoommateCard(
            name = "Roommate 1",
            amount = "$634",
            amountColor = Color(0xFFB00020), // Standardized Red
            isOwed = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        RoommateCard(
            name = "Roommate 2",
            amount = "0",
            amountColor = MaterialTheme.colorScheme.onSurface,
            isOwed = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        RoommateCard(
            name = "Roommate 3",
            amount = "$844",
            amountColor = Color(0xFF4CAF50), // Standardized Green
            isOwed = false
        )
    }
}

@Composable
fun RoommateCard(
    name: String,
    amount: String,
    amountColor: Color,
    isOwed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gray profile placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isOwed) "Amount owed: $amount" else "Amount owing: $amount",
                    fontSize = 14.sp,
                    color = amountColor
                )
            }
        }

        // Dollar sign circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransactionHistorySection(
    onAddBillClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Section header with Add Bill button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            SectionHeader(
                title = "Transaction History (734)",
                actionText = "+ Add Bill",
                onAction = onAddBillClick
            )

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction items
        TransactionItem(
            title = "Roommate #1 Sent Money",
            date = "June 20",
            amount = "$100"
        )

        Spacer(modifier = Modifier.height(8.dp))

        TransactionItem(
            title = "Disneyland",
            date = "June 16",
            amount = "$6434.53"
        )

        Spacer(modifier = Modifier.height(8.dp))

        TransactionItem(
            title = "Electricity",
            date = "June 12",
            amount = "$65.34"
        )

        Spacer(modifier = Modifier.height(8.dp))

        TransactionItem(
            title = "Backyard Cannon",
            date = "June 6",
            amount = "$893.84"
        )
    }
}

@Composable
fun TransactionItem(
    title: String,
    date: String,
    amount: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gray placeholder box
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$date | $amount",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Trash icon with dotted circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ViewMoreButton() {
    Button(
        onClick = { /* to be implemented */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text( "View More")
    }
}