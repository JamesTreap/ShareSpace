package com.example.sharespace.ui.screens.room

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.core.ui.components.NavigationHeader
import com.example.sharespace.room.viewmodel.EditRoomViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoomScreen(
    viewModel: EditRoomViewModel = viewModel(factory = EditRoomViewModel.Factory),
    onNavigateBack: () -> Unit,
    onUpdateSuccessAndNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle initial loading error
    LaunchedEffect(uiState.initialLoadError) {
        uiState.initialLoadError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long // Long duration for initial load errors
                )
                // Optionally navigate back if it's a critical error like missing room ID
            }
        }
    }

    // Handle update error messages
    LaunchedEffect(uiState.updateError) {
        uiState.updateError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error, duration = SnackbarDuration.Short
                )
                viewModel.consumeUpdateError() // Consume error after showing
            }
        }
    }

    // Handle successful update
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            scope.launch {
                keyboardController?.hide()
                snackbarHostState.showSnackbar(
                    message = "Room details updated successfully!",
                    duration = SnackbarDuration.Short
                )
                viewModel.consumeUpdateSuccess() // Consume success state
//                onUpdateSuccessAndNavigateBack() // Navigate back after success
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
            NavigationHeader(
                title = "Edit Room Details", onNavigateBack = onNavigateBack
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (uiState.isLoadingCurrentDetails) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.initialLoadError != null && uiState.currentRoomDetails == null) {
            // Show a prominent error if details couldn't be loaded at all
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.initialLoadError ?: "Failed to load room information.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()), // Make content scrollable
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = uiState.roomName,
                    onValueChange = viewModel::onRoomNameChange,
                    label = { Text("Room Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.updateError?.contains("name", ignoreCase = true) == true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next
                    ),
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp), // Make it a bit taller for description
                    isError = uiState.updateError?.contains(
                        "description", ignoreCase = true
                    ) == true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = viewModel::onAddressChange,
                    label = { Text("Address (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.updateError?.contains("address", ignoreCase = true) == true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done // Last field
                    ),
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.saveRoomChanges()
                    },
                    enabled = !uiState.isUpdating && uiState.initialLoadError == null, // Enable if not updating and no initial fatal error
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Changes")
                    }
                }

                Button(
                    onClick = {
                        keyboardController?.hide()
                    },
                    enabled = !uiState.isUpdating && uiState.initialLoadError == null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    if (uiState.isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Text("Leave Room")
                    }
                }
            }
        }
    }
}
