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

data class UiBill(
    val id: Int,
    val title: String,
    val amount: Double,
    val payerUserId: Int,
    val users: List<UiUserShare>
)

data class UiUserShare(val userId: Int, val amountDue: Double)

data class UiRoommate(val id: Int, val name: String)

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
                        UiRoommate(id = userFromViewModel.id, name = userFromViewModel.name)
                    }

                    is RoomSummaryRoommatesUiState.Error -> {
                        Log.e("RecentBillsSection", "Error loading roommates.")
                        emptyList()
                    }

                    is RoomSummaryRoommatesUiState.Loading -> {
                        Log.d("RecentBillsSection", "Roommates are loading.")
                        emptyList()
                    }
                }
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
                        null
                    }
                }


                if (adaptedBills.isEmpty() && billsUiState.bills.isNotEmpty()) {
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
                }
                if (billsUiState.bills.isNotEmpty()) {
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
fun BillsLazyRow(
    bills: List<UiBill>,
    roommates: List<UiRoommate>,
    currentUserId: Int,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(relevantBills, key = { it.id }) { bill ->
            val share = bill.users.first { it.userId == currentUserId }
            val payerName = roommateMap[bill.payerUserId]?.name ?: "Roommate" // Default name

            BillCard(
                title = bill.title,
                amount = share.amountDue,
                owingTo = payerName,
                onPayClick = onPayClick
            )
        }
    }
}

@Composable
private fun BillCard(
    title: String,
    amount: Double,
    owingTo: String,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp), modifier = modifier, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
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
