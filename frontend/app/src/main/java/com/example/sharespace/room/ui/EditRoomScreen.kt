package com.example.sharespace.ui.screens.room

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sharespace.core.ui.components.NavigationHeader

@Composable
fun EditRoomScreen( onNavigateBack: (() -> Unit)? = null,) {
    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Edit Room",
                onNavigateBack = onNavigateBack
            )
        },
        modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                text = "Edit room screen"
            )
        }
    }
}