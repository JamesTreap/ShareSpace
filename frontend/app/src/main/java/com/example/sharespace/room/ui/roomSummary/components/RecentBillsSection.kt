package com.example.sharespace.room.ui.roomSummary.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.components.SectionHeader
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
    onPayBill: () -> Unit,
    onViewAllBills: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        SectionHeader(
            title = "Recent Bills You Owe",
            actionText = "+ Add Bill",
            onAction = onAddBill,
            modifier = Modifier.fillMaxWidth()
        )
        when (billsUiState) {
            is BillsUiState.Loading -> Box(
                Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
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
                            .height(100.dp), contentAlignment = Alignment.Center
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
                        onPayClick = onPayBill
                    )
                }
                Spacer(Modifier.height(8.dp))
                val relevantBillsExist = adaptedBills.any { bill ->
                    bill.payerUserId != currentUserId && bill.users.any { it.userId == currentUserId && it.amountDue > 0.0 }
                }
                if (!relevantBillsExist && adaptedBills.isNotEmpty()) {
                    // Bills exist in the room and were adapted, but none are owed by the current user.
                    Column(
                        Modifier.fillMaxWidth(),
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
                    Button(
                        onClick = onViewAllBills,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
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
            .height(150.dp),
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
            .height(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No bills added to the room yet.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        StyledButton(onClick = onAdd, text = "Add First Bill")
    }
}

@Composable
fun BillsLazyRow( // Takes UiBill, UiRoommate
    bills: List<UiBill>,
    roommates: List<UiRoommate>,
    currentUserId: Int,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier // Added modifier
) {
    val roommateMap = remember(roommates) { roommates.associateBy { it.id } }

    val relevantBills = remember(bills, currentUserId) {
        bills.filter { bill ->
            bill.payerUserId != currentUserId && bill.users.any { it.userId == currentUserId && it.amountDue > 0.0 }
        }
    }

    if (relevantBills.isEmpty()) {
        return
    }

    LazyRow(
        modifier = modifier, // Apply modifier
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(relevantBills, key = { it.id }) { bill ->
            // Find the current user's share. It's guaranteed to exist due to the filter.
            val share = bill.users.first { it.userId == currentUserId }
            val payerName = roommateMap[bill.payerUserId]?.name ?: "Roommate" // Default name

            BillCard(
                title = bill.title,
                amount = share.amountDue, // This is the amount the current user owes for this bill
                owingTo = payerName,
                onPayClick = onPayClick
            )
        }
    }
}

@Composable
private fun BillCard(
    title: String, amount: Double, // This is the specific amount owed by the current user
    owingTo: String, onPayClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp), modifier = modifier, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Set the container color here
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween // This might need adjustment if the content height varies
        ) {
            Column { // This inner column groups the text content
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$" + "%.2f".format(amount),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Owing to $owingTo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Text color should provide good contrast
                )
            }
            Spacer(Modifier.height(8.dp)) // Add some space before the button, especially if content above is short
            Button(
                onClick = onPayClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pay User")
            }
        }
    }
}
