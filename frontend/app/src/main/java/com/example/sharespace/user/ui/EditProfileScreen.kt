package com.example.sharespace.user.ui

import ScreenHeader
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.core.ui.theme.AlertRed
import com.example.sharespace.core.ui.theme.AquaAccent
import com.example.sharespace.core.ui.theme.TextSecondary
import com.example.sharespace.user.viewmodel.EditProfileScreenViewModel


@SuppressLint("DiscouragedApi")
@Composable
fun EditProfileScreen(
    viewModel: EditProfileScreenViewModel = viewModel(factory = EditProfileScreenViewModel.Factory),
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val user by viewModel.user
    val name by viewModel.name
    val username by viewModel.username
    val selectedIconIndex by viewModel.selectedIconIndex
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    viewModel.loadData()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp), snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState, snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = AlertRed,
                        contentColor = Color.White
                    )
                })
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ScreenHeader(
                title = "Edit Profile", onNavigateBack, actions = {}, photoUrl = user?.photoUrl
            )
            HorizontalDivider(
                color = Color.LightGray.copy(alpha = 0.8f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 18.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                Modifier
                    .padding(vertical = 0.dp, horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

//                Spacer(modifier = Modifier.height(8.dp))
//
//                OutlinedTextField(
//                    value = username,
//                    onValueChange = { viewModel.onUsernameChange(it) },
//                    label = { Text("Username") },
//                    modifier = Modifier.fillMaxWidth()
//                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Selected Icon",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.height(160.dp)
                ) {
                    items(10) { index ->
                        val resId = remember {
                            context.resources.getIdentifier(
                                "pfp$index", "drawable", context.packageName
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 4.dp,
                                    color = if (index == selectedIconIndex) AquaAccent else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.onIconSelected(index) }) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Profile Picture $index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.updateProfile(onNavigateBack, snackbarHostState)
                    }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Profile")
                }
            }
        }
    }


}