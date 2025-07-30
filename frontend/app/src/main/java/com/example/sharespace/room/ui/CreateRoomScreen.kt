package com.example.sharespace.ui.screens.room

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import com.example.sharespace.core.ui.components.ButtonType
import com.example.sharespace.core.ui.components.NavigationHeader
import com.example.sharespace.core.ui.components.StyledButton
import com.example.sharespace.core.ui.components.StyledTextField
import com.example.sharespace.room.viewmodel.CreateRoomViewModel
import kotlinx.coroutines.launch

@Composable
fun CreateRoomScreen(
    viewModel: CreateRoomViewModel = viewModel(factory = CreateRoomViewModel.Factory),
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState = viewModel.uiState
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.roomCreateSuccess) {
        if (uiState.roomCreateSuccess) {
            scope.launch {
                keyboardController?.hide()
                snackbarHostState.showSnackbar(
                    message = "Room details updated successfully!",
                    duration = SnackbarDuration.Short
                )
                viewModel.consumeUpdateSuccess()
//                onUpdateSuccessAndNavigateBack() // Navigate back after success
            }
        }
    }

    Scaffold(
        topBar = {
            NavigationHeader(
                title = "Create Room", onNavigateBack = onNavigateBack
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Make content scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StyledTextField(
                value = uiState.roomName,
                onValueChange = viewModel::onRoomNameChange,
                label = { Text("Room Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.roomCreateError?.contains("name", ignoreCase = true) == true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next
                ),
            )
//            Spacer(Modifier.height(8.dp))

            StyledTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.roomCreateError?.contains(
                    "description", ignoreCase = true
                ) == true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
            )
            Spacer(Modifier.height(16.dp))

            StyledButton(
                onClick = {
                    keyboardController?.hide()
                    viewModel.createRoom()
                },
                text = "Create Room",
                buttonType = ButtonType.Primary,
                enabled = uiState.roomName.isNotEmpty(),
                loading = uiState.isUpdating,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}