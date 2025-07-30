package com.example.sharespace.room.ui.roomSummary.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.components.StyledButton
import com.example.sharespace.room.viewmodel.BillsUiState
import com.example.sharespace.room.viewmodel.RoomSummaryRoommatesUiState

data class UiBill( // Renamed to avoid conflict with ViewModelBill
    val id: Int,
    val title: String,
    val amount: Double, // This is the total bill amount, not the share
    val payerUserId: Int,
    val users: List<UiUserShare>
)

data class UiUserShare(val userId: Int, val amountDue: Double)

data class UiRoommate(val id: Int, val name: String) // Renamed

// --- Main Composable ---
@Composable
fun RecentBillsSection(
    billsUiState: BillsUiState,
    roommatesUiState: RoomSummaryRoommatesUiState,
    currentUserId: Int?,
    onAddBill: () -> Unit,
    onPayBill: (billId: Int, amount: Double) -> Unit,
    onViewAllBills: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Recent Bills You Owe",
            actionText = "+ Add Bill",
            onAction = onAddBill,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        when (billsUiState) {
            is BillsUiState.Loading -> Box(
                Modifier
                    .fillMaxWidth()
                    .height(212.dp), // Approx height for BillCard in LazyRow + padding
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is BillsUiState.Error -> BillsErrorMini(onRetry, billsUiState.message)

            is BillsUiState.Empty -> BillsEmptyMini(onAddBill)

            is BillsUiState.Success -> {
                if (currentUserId == null) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(212.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("User information not available.") }
                    return@Column
                }

                val uiRoommates = when (roommatesUiState) {
                    is RoomSummaryRoommatesUiState.Success -> roommatesUiState.roommates.map { userFromViewModel ->
                        // userFromViewModel is of type com.example.sharespace.core.domain.model.User
                        UiRoommate(id = userFromViewModel.id, name = userFromViewModel.name)
                    }
                    is RoomSummaryRoommatesUiState.Error -> {
                        // Optionally handle error state for roommates, e.g., show a message or use empty list
                        Log.e("RecentBillsSection", "Error loading roommates.")
                        emptyList()
                    }
                    is RoomSummaryRoommatesUiState.Loading -> {
                        // Optionally handle loading state for roommates, e.g., show a placeholder or use empty list
                        Log.d("RecentBillsSection", "Roommates are loading.")
                        emptyList() // Or potentially show a different UI element if roommates are critical here
                    }
                }

                // Adapt ViewModelBill to UiBill
                val adaptedBills = billsUiState.bills.mapNotNull { viewModelBill ->
                    val userShares = viewModelBill.metadata?.users?.map { userDue ->
                        UiUserShare(userId = userDue.userId, amountDue = userDue.amountDue)
                    }
                    if (userShares != null) {
                        UiBill(
                            id = viewModelBill.id,
                            title = viewModelBill.title,
                            amount = viewModelBill.amount, // Total bill amount
                            payerUserId = viewModelBill.payerUserId,
                            users = userShares
                        )
                    } else {
                        null // Or handle bills without metadata differently
                    }
                }


                if (adaptedBills.isEmpty() && billsUiState.bills.isNotEmpty()) {
                    // This means there were bills, but none could be adapted (e.g. all missing metadata)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No bill details available to display.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    // Call the provided BillsLazyRow with adapted data
                    BillsLazyRow(
                        bills = adaptedBills,
                        roommates = uiRoommates,
                        currentUserId = currentUserId,
                        onPayClick = onPayBill // Pass the onPayBill from ViewModel
                    )
                }


                val relevantBillsExist = adaptedBills.any { bill ->
                    bill.payerUserId != currentUserId &&
                            bill.users.any { it.userId == currentUserId && it.amountDue > 0.0 }
                }

                if (!relevantBillsExist && adaptedBills.isNotEmpty()) {
                    // Bills exist in the room and were adapted, but none are owed by the current user.
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp) // Appears below the (empty) LazyRow
                            .heightIn(min = 100.dp), // Give some space if LazyRow is empty
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No bills currently require your payment.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else if (adaptedBills.isEmpty() && billsUiState.bills.isEmpty()) {
                    // This case is already handled by BillsUiState.Empty, but as a fallback.
                    // BillsEmptyMini is shown above.
                }


                // "View All Room Bills" button
                if (billsUiState.bills.isNotEmpty()) { // Show if any bills exist in the room
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onViewAllBills,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("View All Room Bills")
                    }
                }
            }
        }
    }
}

@Composable
private fun BillsErrorMini(onRetry: () -> Unit, message: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun BillsEmptyMini(onAdd: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No bills added to the room yet.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        StyledButton(onClick = onAdd, text = "Add First Bill")
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onAction) {
            Text(actionText, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BillsLazyRow( // Takes UiBill, UiRoommate
    bills: List<UiBill>,
    roommates: List<UiRoommate>,
    currentUserId: Int,
    onPayClick: (billId: Int, amount: Double) -> Unit,
    modifier: Modifier = Modifier // Added modifier
) {
    val roommateMap = remember(roommates) { roommates.associateBy { it.id } }

    val relevantBills = remember(bills, currentUserId) {
        bills.filter { bill ->
            bill.payerUserId != currentUserId &&
                    bill.users.any { it.userId == currentUserId && it.amountDue > 0.0 }
        }
    }

    if (relevantBills.isEmpty()) {
        return
    }

    LazyRow(
        modifier = modifier, // Apply modifier
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(relevantBills, key = { it.id }) { bill ->
            // Find the current user's share. It's guaranteed to exist due to the filter.
            val share = bill.users.first { it.userId == currentUserId }
            val payerName = roommateMap[bill.payerUserId]?.name ?: "Roommate" // Default name

            BillCard(
                title = bill.title,
                amount = share.amountDue, // This is the amount the current user owes for this bill
                owingTo = payerName,
                onPayClick = { onPayClick(bill.id, share.amountDue) }
            )
        }
    }
}

@Composable
private fun BillCard(
    title: String,
    amount: Double, // This is the specific amount owed by the current user
    owingTo: String,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Added subtle elevation
        modifier = modifier
            .width(160.dp)
            .height(180.dp) // Consistent with your BillsUiState.Loading placeholder
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium, // Slightly larger for card title
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis // Handle long titles
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$" + "%.2f".format(amount), // Format to 2 decimal places
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Owing to $owingTo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onPayClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pay User") // Changed from "Pay" for clarity as per reference
            }
        }
    }
}
