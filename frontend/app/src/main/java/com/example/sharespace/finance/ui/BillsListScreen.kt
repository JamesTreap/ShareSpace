package com.example.sharespace.ui.screens.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.components.NavigationHeader

@Composable
fun BillsListScreen(
    onNavigateBack: (() -> Unit)? = null,
    onAddBillClick: () -> Unit = {},
    onFinanceManagerClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Bills", onNavigateBack = onNavigateBack, actions = {
                    IconButton(onClick = onAddBillClick) {
                        Icon(Icons.Default.Add, "Add Bill")
                    }
                })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Bills List", style = MaterialTheme.typography.headlineMedium)

            Button(onClick = onFinanceManagerClick, modifier = Modifier.fillMaxWidth()) {
                Text("Finance Manager")
            }

            Text("Bills list content coming soon...")
        }
    }
}

@Composable
fun EditBillScreen(onNavigateBack: (() -> Unit)? = null) {
    Scaffold(
        topBar = {
            NavigationHeader(title = "Edit Bill", onNavigateBack = onNavigateBack)
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text("Edit Bill Screen - Content Coming Soon")
        }
    }
}