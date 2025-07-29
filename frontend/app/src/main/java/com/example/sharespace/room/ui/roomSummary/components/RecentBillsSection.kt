package com.example.sharespace.room.ui.roomSummary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.domain.model.Bill

@Composable
fun RecentBillsSection(
    bills: List<Bill>, onPay: (Bill) -> Unit, onViewAll: () -> Unit
) {
    // This Column is a fixed part of the main screen layout
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = "Recent Bills", actionText = "View All", onAction = onViewAll)
        if (bills.isEmpty()) {
            Text(
                "No recent bills.",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
            return@Column // Use return@Column to exit this composable's Column
        }
        // LazyRow provides horizontal scrolling for bills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(bills) { bill ->
                ElevatedCard(modifier = Modifier.width(180.dp)) { // Fixed width for bill cards
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(bill.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("$${bill.amount}", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
//                        Text(bill.subtitle, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { onPay(bill) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Pay User")
                        }
                    }
                }
            }
        }
    }
}