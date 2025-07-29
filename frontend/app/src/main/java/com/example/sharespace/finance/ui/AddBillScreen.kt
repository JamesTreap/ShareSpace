package com.example.sharespace.ui.screens.finance

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.R
import com.example.sharespace.core.ui.components.NavigationHeader
import java.text.NumberFormat
import java.util.*



@Composable
fun whiteTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    errorContainerColor = Color.White,
)

@Composable
fun AddBillScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinanceManagerViewModel = viewModel(factory = FinanceManagerViewModel.Factory)
) {
    val roommates by viewModel.addBillRoommates.collectAsState()
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val billCreated by viewModel.billCreated


    // Form state
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }
    var occurs by remember { mutableStateOf("1w") } // Default to weekly
    var repeats by remember { mutableStateOf("1") }

    // Calculate debt-related statistics
    val debtorsCount = roommates.count { it.currentBalance < 0 }
    val hasDebtors = debtorsCount > 0

    LaunchedEffect(billCreated) {
        if (billCreated) {
            onNavigateBack()
            viewModel.resetBillCreated()
        }
    }

    // Handle successful bill creation
    LaunchedEffect(Unit) {
        viewModel.loadRoommatesForBill()
    }

    // Show error messages
    errorMessage?.let { message ->
        LaunchedEffect(message) {
        }
    }

    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Add Bill",
                onNavigateBack = onNavigateBack
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
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show error message if any
                errorMessage?.let { message ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(16.dp),
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }

                // Section for all the bill details
                item {
                    BillInfoSection(
                        title = title,
                        onTitleChange = { title = it },
                        category = category,
                        onCategoryChange = { category = it },
                        totalCost = totalCost,
                        onTotalCostChange = { totalCost = it },
                        occurs = occurs,
                        onOccursChange = { occurs = it },
                        repeats = repeats,
                        onRepeatsChange = { repeats = it }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    SplitMethodSection(
                        totalAmount = totalCost,
                        debtorsCount = debtorsCount,
                        hasDebtors = hasDebtors,
                        onSplitEvenly = { viewModel.splitEvenly(totalCost) }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    RoommateSplitSection(
                        roommates = roommates,
                        onAmountChange = { userId, amount ->
                            viewModel.updateRoommateAmount(userId, amount)
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // Create Bill button
                item {
                    Button(
                        onClick = {
                            viewModel.createBill(
                                title = title,
                                category = category,
                                totalAmount = totalCost,
                                frequency = occurs,
                                repeats = repeats
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealPrimary,
                            contentColor = Color.White
                        ),
                        enabled = !isLoading && title.isNotBlank() && totalCost.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Create Bill", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillInfoSection(
    title: String,
    onTitleChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    totalCost: String,
    onTotalCostChange: (String) -> Unit,
    occurs: String,
    onOccursChange: (String) -> Unit,
    repeats: String,
    onRepeatsChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Title Field
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            placeholder = { Text("e.g., Grocery Shopping") },
            modifier = Modifier.fillMaxWidth(),
            colors = whiteTextFieldColors(),
            trailingIcon = {
                if (title.isNotEmpty()) {
                    IconButton(onClick = { onTitleChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear text")
                    }
                }
            },
            singleLine = true
        )

        // Category Field
        OutlinedTextField(
            value = category,
            onValueChange = onCategoryChange,
            label = { Text("Category") },
            placeholder = { Text("e.g., Food, Utilities, Entertainment") },
            modifier = Modifier.fillMaxWidth(),
            colors = whiteTextFieldColors(),
            singleLine = true
        )

        // Total Cost Field
        OutlinedTextField(
            value = totalCost,
            onValueChange = { value ->
                // Allow numbers with optional decimal places
                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onTotalCostChange(value)
                }
            },
            label = { Text("Total Cost") },
            placeholder = { Text("Enter amount in dollars") },
            modifier = Modifier.fillMaxWidth(),
            colors = whiteTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Text(
                    text = "$",
                    modifier = Modifier.padding(start = 8.dp)
                )
            },
            trailingIcon = {
                if (totalCost.isNotEmpty()) {
                    IconButton(onClick = { onTotalCostChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear text")
                    }
                }
            },
            singleLine = true
        )

        // Occurs and Repeats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = occurs,
                onValueChange = onOccursChange,
                label = { Text("Frequency") },
                placeholder = { Text("1w, 1m, 1d") },
                modifier = Modifier.weight(1f),
                colors = whiteTextFieldColors()
            )
            OutlinedTextField(
                value = repeats,
                onValueChange = { value ->
                    if (value.isEmpty() || value.matches(Regex("^\\d+$"))) {
                        onRepeatsChange(value)
                    }
                },
                label = { Text("Repeats") },
                placeholder = { Text("1") },
                modifier = Modifier.weight(1f),
                colors = whiteTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
fun SplitMethodSection(
    totalAmount: String,
    debtorsCount: Int,
    hasDebtors: Boolean,
    onSplitEvenly: () -> Unit
) {

            OutlinedButton(
                onClick = onSplitEvenly,
                modifier = Modifier.fillMaxWidth(),
                enabled = totalAmount.isNotBlank()
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Split Evenly Among All")
            }
}

@Composable
fun RoommateSplitSection(
    roommates: List<RoommateSplit>,
    onAmountChange: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Roommate Splits",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (roommates.isEmpty()) {
            Text(
                text = "Loading roommates...",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            roommates.forEach { split ->
                EnhancedRoommateSplitItem(
                    roommateSplit = split,
                    onAmountChange = { amount ->
                        onAmountChange(split.user.id, amount)
                    }
                )
            }
        }
    }
}

@Composable
fun EnhancedRoommateSplitItem(
    roommateSplit: RoommateSplit,
    onAmountChange: (String) -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                roommateSplit.currentBalance > 0 -> Color(0xFF4CAF50).copy(alpha = 0.1f) // They owe you
                roommateSplit.currentBalance < 0 -> Color(0xFFF44336).copy(alpha = 0.1f) // You owe them
                else -> Color.White
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = roommateSplit.user.name,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = roommateSplit.user.name ?: roommateSplit.user.username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Show current balance
                    if (roommateSplit.currentBalance != 0.0) {
                        Text(
                            text = when {
                                roommateSplit.currentBalance > 0 -> "Owes you ${currencyFormatter.format(roommateSplit.currentBalance)}"
                                else -> "You owe ${currencyFormatter.format(-roommateSplit.currentBalance)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                roommateSplit.currentBalance > 0 -> Color(0xFF4CAF50)
                                else -> Color(0xFFF44336)
                            }
                        )
                    } else {
                        Text(
                            text = "Even",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Amount input
            OutlinedTextField(
                value = roommateSplit.amount,
                onValueChange = { value ->
                    // Allow numbers with optional decimal places
                    if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                        onAmountChange(value)
                    }
                },
                label = { Text("Amount") },
                modifier = Modifier.width(120.dp),
                colors = whiteTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Text(
                        text = "$",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                singleLine = true
            )
        }
    }
}