package com.example.sharespace.ui.screens.finance

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.R
import com.example.sharespace.core.ui.components.NavigationHeader
import com.example.sharespace.core.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.*

// Define your app's specific colors
val TealPrimary = Color(0xFF4DB6AC)
val LightGreyBackground = Color(0xFFF5F5F5)
val LightRed = Color(0xFFE57373)
val LightGrayBorder = Color.LightGray.copy(alpha = 0.5f)

@Composable
fun FinanceManagerScreen(
    onNavigateBack: () -> Unit,
    onAddBillClick: () -> Unit,
    viewModel: FinanceManagerViewModel = viewModel(factory = FinanceManagerViewModel.Factory)
) {
    val transactions by viewModel.transactions.collectAsState()
    val roommates by viewModel.roommates.collectAsState()
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    // State for expanding/collapsing transaction list
    var showAllTransactions by remember { mutableStateOf(false) }

    // Load transactions when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }

    // Show error messages as snackbar or dialog
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // You can implement a snackbar here if needed
            // For now, we'll just log it and clear after showing
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Bill Overview",
                onNavigateBack = onNavigateBack
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (isLoading) {
            // Show loading indicator
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TealPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(LightGreyBackground)
                    .padding(horizontal = 16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { BillSummarySection(transactions) }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { RoommatesSection(roommates = roommates) }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    TransactionHistorySection(
                        transactions = transactions,
                        showAllTransactions = showAllTransactions,
                        onAddBillClick = onAddBillClick,
                        onDeleteTransaction = { transactionId, transactionType ->
                            viewModel.deleteTransaction(transactionId, transactionType)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ViewMoreButton(
                        transactions = transactions,
                        showAllTransactions = showAllTransactions,
                        onToggleShowAll = { showAllTransactions = !showAllTransactions }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun TransactionHistorySection(
    transactions: List<com.example.sharespace.core.data.repository.dto.finance.ApiTransaction>,
    showAllTransactions: Boolean,
    onAddBillClick: () -> Unit,
    onDeleteTransaction: (Int, String) -> Unit
) {
    // Determine which transactions to show
    val transactionsToShow = if (showAllTransactions) {
        transactions
    } else {
        transactions.take(5) // Show only latest 5
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Using SectionHeader component
        SectionHeader(
            title = "Transaction History (${transactions.size})",
            actionText = "+ Add Bill",
            onAction = onAddBillClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            // Show empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions yet\nTap + Add Bill to get started",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            // Show filtered transactions with null safety
            transactionsToShow.forEach { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onDelete = { onDeleteTransaction(transaction.id, transaction.type) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Show indicator if there are more transactions but we're not showing all
            if (!showAllTransactions && transactions.size > 5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Showing 5 of ${transactions.size} transactions",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ViewMoreButton(
    transactions: List<com.example.sharespace.core.data.repository.dto.finance.ApiTransaction>,
    showAllTransactions: Boolean,
    onToggleShowAll: () -> Unit
) {
    // Only show the button if there are more than 5 transactions
    if (transactions.size <= 5) return

    Button(
        onClick = onToggleShowAll,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (showAllTransactions)
                MaterialTheme.colorScheme.outline else TealPrimary,
            contentColor = if (showAllTransactions)
                MaterialTheme.colorScheme.onSurface else Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (showAllTransactions)
                    Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (showAllTransactions)
                    "Show Less" else "View More (${transactions.size - 5} more)",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun BillSummarySection(transactions: List<com.example.sharespace.core.data.repository.dto.finance.ApiTransaction>) {
    // Calculate total from real transactions
    val totalAmount = transactions.sumOf { it.amount }

    // Group by category for breakdown - handle null categories
    val categoryTotals = transactions
        .groupBy { it.category ?: "Unknown" } // Handle null categories
        .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Updated circular amount display to be a hollow ring
        Box(
            modifier = Modifier
                .size(140.dp)
                .border(
                    width = 8.dp,
                    color = TealPrimary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$%.2f".format(totalAmount),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Bill breakdown from real data
        Column {
            if (categoryTotals.isEmpty()) {
                Text(
                    text = "No transactions yet",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                categoryTotals.entries.take(4).forEach { (category, amount) ->
                    BillBreakdownItem(
                        category = category.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }, // Safe capitalize alternative
                        amount = "$%.2f".format(amount)
                    )
                }
            }
        }
    }
}

@Composable
fun BillBreakdownItem(category: String, amount: String) {
    Text(
        text = "$category - $amount",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun RoommatesSection(roommates: List<com.example.sharespace.core.data.repository.dto.users.ApiUser>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Roommates (${roommates.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (roommates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No roommates found",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            roommates.forEach { roommate ->
                RoommateCard(
                    name = roommate.name ?: roommate.username, // Use name or fallback to username
                    amountOwed = "$0", // TODO: Calculate real amounts from transactions
                    imageRes = R.drawable.ic_launcher_background // TODO: Use real profile images
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun RoommateCard(
    name: String,
    imageRes: Int,
    amountOwed: String? = null,
    amountOwing: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightGrayBorder, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                when {
                    amountOwed != null -> Text(
                        text = "Amount owed: $amountOwed",
                        fontSize = 14.sp,
                        color = if (amountOwed != "$0") LightRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    amountOwing != null -> Text(
                        text = "Amount owing: $amountOwing",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "View Details",
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun TransactionItem(
    transaction: com.example.sharespace.core.data.repository.dto.finance.ApiTransaction,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightGrayBorder, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = when (transaction.type) {
                    "bill" -> Icons.Default.MailOutline
                    "payment" -> Icons.Default.ShoppingCart
                    else -> Icons.Default.ArrowForward
                },
                contentDescription = transaction.title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = transaction.title ?: "Untitled Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${formatDate(transaction.createdAt)} | $%.2f".format(transaction.amount),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete", color = Color.Red) },
                    onClick = {
                        expanded = false
                        showDeleteDialog = true
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction") },
            text = {
                Text("Are you sure you want to delete \"${transaction.title ?: "this transaction"}\"? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper function to format date
private fun formatDate(isoString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val date = inputFormat.parse(isoString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        // Fallback if parsing fails
        isoString.substring(0, 10) // Just show date part
    }
}