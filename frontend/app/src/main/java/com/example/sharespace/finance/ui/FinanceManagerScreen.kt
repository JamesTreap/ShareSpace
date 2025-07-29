package com.example.sharespace.ui.screens.finance

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.R
import com.example.sharespace.core.data.repository.dto.finance.ApiBill
import com.example.sharespace.core.ui.components.NavigationHeader
import com.example.sharespace.core.ui.components.SectionHeader
import com.example.sharespace.core.ui.components.StyledButton
import com.example.sharespace.core.ui.components.ButtonType
import com.example.sharespace.ui.screens.finance.components.DebtSummarySection
import com.example.sharespace.ui.screens.finance.components.DebtDetailsDialog
import java.text.SimpleDateFormat
import java.util.*

// Define your app's specific colors
val TealPrimary = Color(0xFF4DB6AC)
val LightGreyBackground = Color(0xFFF5F5F5)
val LightRed = Color(0xFFE57373)
val LightGreen = Color(0xFF81C784)
val LightGrayBorder = Color.LightGray.copy(alpha = 0.5f)

@Composable
fun FinanceManagerScreen(
    onNavigateBack: () -> Unit,
    onAddBillClick: () -> Unit,
    viewModel: FinanceManagerViewModel = viewModel(factory = FinanceManagerViewModel.Factory)
) {
    val transactions by viewModel.transactions.collectAsState()
    val roommates by viewModel.roommates.collectAsState()
    val debtSummaries by viewModel.debtSummaries.collectAsState()
    val roomMembersWithDebts by viewModel.roomMembersWithDebts.collectAsState()
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    // State for expanding/collapsing transaction list
    var showAllTransactions by remember { mutableStateOf(false) }

    // State for payment dialog
    var selectedPayee by remember { mutableStateOf<com.example.sharespace.core.data.repository.dto.users.ApiUser?>(null) }

    // State for debt details dialog
    var showDebtDetailsDialog by remember { mutableStateOf(false) }

    // State for preselected payment values (when clicking Pay from debt summary)
    var preselectedPayeeId by remember { mutableStateOf<Int?>(null) }
    var preselectedPayeeName by remember { mutableStateOf("") }

    // Load data when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
        viewModel.loadRoomMembersWithDebts()
    }

    // Show error messages
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Finance Manager",
                onNavigateBack = onNavigateBack,
                actions = {
                    // Debt details button
                    IconButton(onClick = { showDebtDetailsDialog = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Debt Details",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Refresh button
                    IconButton(onClick = {
                        viewModel.loadTransactions()
                        viewModel.loadRoomMembersWithDebts()
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (isLoading) {
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

                // Debt Summary Section (NEW - replaces old balance calculation)
                item {
                    DebtSummarySection(
                        debtSummaries = debtSummaries,
                        onPayUser = { userId, userName ->
                            // Find the actual user object for the payment dialog
                            val payeeUser = roommates.find { it.id == userId }
                            if (payeeUser != null) {
                                selectedPayee = payeeUser
                                preselectedPayeeId = userId
                                preselectedPayeeName = userName
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Bill Summary Section
                item { BillSummarySection(transactions) }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Action Buttons Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StyledButton(
                            onClick = onAddBillClick,
                            text = "Add Bill",
                            buttonType = ButtonType.Primary,
                            icon = Icons.Default.Add,
                            modifier = Modifier.weight(1f)
                        )

                        StyledButton(
                            onClick = {
                                // Show payment dialog without preselected user
                                selectedPayee = roommates.firstOrNull()
                                preselectedPayeeId = null
                                preselectedPayeeName = ""
                            },
                            text = "Make Payment",
                            buttonType = ButtonType.Secondary,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Transaction History Section
                item {
                    TransactionHistorySection(
                        transactions = transactions,
                        showAllTransactions = showAllTransactions,
                        onDeleteTransaction = { transactionId, transactionType ->
                            viewModel.deleteTransaction(transactionId, transactionType)
                        }
                    )
                }

                // View More Button
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

    // Payment Dialog
    selectedPayee?.let { payee ->
        PaymentDialog(
            payee = payee,
            roommates = roommates,
            preselectedPayeeId = preselectedPayeeId,
            onDismiss = {
                selectedPayee = null
                preselectedPayeeId = null
                preselectedPayeeName = ""
            },
            onPayment = { payeeId, amount, description ->
                viewModel.createPayment(payeeId, amount, description)
                selectedPayee = null
                preselectedPayeeId = null
                preselectedPayeeName = ""
            },
            isLoading = isLoading
        )
    }

    // Debt Details Dialog
    if (showDebtDetailsDialog) {
        DebtDetailsDialog(
            isVisible = showDebtDetailsDialog,
            onDismiss = { showDebtDetailsDialog = false },
            roomMembersWithDebts = roomMembersWithDebts,
            currentUserId = 1 // TODO: Get this from user session repository
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    payee: com.example.sharespace.core.data.repository.dto.users.ApiUser,
    roommates: List<com.example.sharespace.core.data.repository.dto.users.ApiUser>,
    preselectedPayeeId: Int? = null,
    onDismiss: () -> Unit,
    onPayment: (Int, String, String) -> Unit, // payeeId, amount, description
    isLoading: Boolean = false
) {
    var selectedPayeeId by remember(preselectedPayeeId) {
        mutableStateOf(preselectedPayeeId ?: payee.id)
    }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val selectedPayeeName = roommates.find { it.id == selectedPayeeId }?.let {
        it.name ?: it.username
    } ?: "Unknown User"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Make Payment")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Payee selection dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedPayeeName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Pay to") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roommates.forEach { roommate ->
                            DropdownMenuItem(
                                text = { Text(roommate.name ?: roommate.username) },
                                onClick = {
                                    selectedPayeeId = roommate.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { value ->
                        // Allow numbers with optional decimal places
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amount = value
                        }
                    },
                    label = { Text("Amount") },
                    placeholder = { Text("Enter amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = {
                        Text(
                            text = "$",
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("What's this payment for?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount.isNotBlank()) {
                        val finalDescription = if (description.isBlank()) {
                            "Payment to $selectedPayeeName"
                        } else {
                            description
                        }
                        onPayment(selectedPayeeId, amount, finalDescription)
                    }
                },
                enabled = amount.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send Payment")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TransactionHistorySection(
    transactions: List<ApiBill>,
    showAllTransactions: Boolean,
    onDeleteTransaction: (Int, String) -> Unit
) {
    // Determine which transactions to show
    val transactionsToShow = if (showAllTransactions) {
        transactions
    } else {
        transactions.take(5) // Show only latest 5
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaction History (${transactions.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

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
                    text = "No transactions yet\nAdd bills or make payments to get started",
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
    transactions: List<ApiBill>,
    showAllTransactions: Boolean,
    onToggleShowAll: () -> Unit
) {
    // Only show the button if there are more than 5 transactions
    if (transactions.size <= 5) return

    StyledButton(
        onClick = onToggleShowAll,
        text = if (showAllTransactions)
            "Show Less" else "View More (${transactions.size - 5} more)",
        buttonType = if (showAllTransactions) ButtonType.Secondary else ButtonType.Primary,
        icon = if (showAllTransactions)
            Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BillSummarySection(transactions: List<ApiBill>) {
    // Calculate total from real transactions
    val billTransactions = transactions.filter { it.type == "bill" }
    val totalAmount = billTransactions.sumOf { it.amount }

    // Group by category for breakdown - handle null categories
    val categoryTotals = billTransactions
        .groupBy { it.category ?: "Unknown" } // Handle null categories
        .mapValues { (_, billTransactions) -> billTransactions.sumOf { it.amount } }

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
fun TransactionItem(
    transaction: ApiBill,
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
                    "payment" -> Icons.Default.CheckCircle
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
                StyledButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    text = "Delete",
                    buttonType = ButtonType.Danger
                )
            },
            dismissButton = {
                StyledButton(
                    onClick = { showDeleteDialog = false },
                    text = "Cancel",
                    buttonType = ButtonType.Tertiary
                )
            }
        )
    }
}

@Composable
fun RoommatesSection(
    roommates: List<com.example.sharespace.core.data.repository.dto.users.ApiUser>,
    debtSummaries: List<com.example.sharespace.core.data.repository.dto.users.DebtSummary>,
    onPayClick: (com.example.sharespace.core.data.repository.dto.users.ApiUser) -> Unit
) {
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
                // Find debt summary for this roommate
                val debtSummary = debtSummaries.find { it.userId == roommate.id }
                val balance = debtSummary?.netBalance ?: 0.0

                RoommateCard(
                    roommate = roommate,
                    balance = balance,
                    onPayClick = { onPayClick(roommate) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun RoommateCard(
    roommate: com.example.sharespace.core.data.repository.dto.users.ApiUser,
    balance: Double,
    onPayClick: () -> Unit
) {
    val balanceText = when {
        balance > 0 -> "Owes you: ${String.format("%.2f", balance)}"
        balance < 0 -> "You owe: ${String.format("%.2f", -balance)}"
        else -> "Even"
    }

    val balanceColor = when {
        balance > 0 -> LightGreen
        balance < 0 -> LightRed
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightGrayBorder, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = roommate.name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = roommate.name ?: roommate.username,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = balanceText,
                    fontSize = 14.sp,
                    color = balanceColor,
                    fontWeight = if (balance != 0.0) FontWeight.Medium else FontWeight.Normal
                )
            }
        }

        // Show pay button only if current user owes this roommate money
        if (balance < 0) {
            Button(
                onClick = onPayClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Pay", fontSize = 14.sp)
            }
        } else {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.outline
            )
        }
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