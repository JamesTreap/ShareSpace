@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sharespace.room.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.core.ui.components.NavigationHeader
import com.example.sharespace.room.viewmodel.AddRoommateViewModel
import kotlinx.coroutines.launch

@Composable
fun AddRoommateScreen(
    viewModel: AddRoommateViewModel = viewModel(factory = AddRoommateViewModel.Factory),
    onNavigateBack: () -> Unit,
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState.inviteError) {
        uiState.inviteError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error, duration = SnackbarDuration.Short
                )
            }
        }
    }

    LaunchedEffect(uiState.inviteSuccess) {
        if (uiState.inviteSuccess) {
            scope.launch {
                keyboardController?.hide()
                snackbarHostState.showSnackbar(
                    message = "Invite sent successfully!", duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        NavigationHeader(
            title = "Add Roommate", onNavigateBack = onNavigateBack
        )
    }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.onUsernameChange(it) },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.inviteError != null && !uiState.isSendingInvite,
                trailingIcon = {
                    if (uiState.username.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearUsername() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear username"
                            )
                        }
                    }
                },
                supportingText = {
                    if (uiState.inviteError != null && !uiState.isSendingInvite) {
                        Text(uiState.inviteError, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.sendInvite()
                },
                enabled = uiState.username.isNotBlank() && !uiState.isSendingInvite,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSendingInvite) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send Invite")
                }
            }
        }
    }
}

