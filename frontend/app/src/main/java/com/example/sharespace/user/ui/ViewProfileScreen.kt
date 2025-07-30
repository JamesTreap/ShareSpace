package com.example.sharespace.user.ui

import ScreenHeader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.core.ui.components.ButtonType
import com.example.sharespace.core.ui.components.StyledButton
import com.example.sharespace.core.ui.theme.TextSecondary
import com.example.sharespace.user.viewmodel.ViewProfileScreenViewModel


@Composable
fun ViewProfileScreen(
    viewModel: ViewProfileScreenViewModel = viewModel(factory = ViewProfileScreenViewModel.Factory),
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {

    val user by viewModel.user
    viewModel.loadData()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ScreenHeader(
                title = user?.name ?: "View Profile",
                onBackClick = onNavigateBack,
                actions = {},
                photoUrl = user?.photoUrl
            )

            HorizontalDivider(
                color = Color.LightGray.copy(alpha = 0.8f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 18.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))


            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "User ID",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    val idText = user?.id.toString()
                    Text(idText, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Username",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        user?.username ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StyledButton(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth(),
                    text = "Edit Profile"
                )

                StyledButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    text = "Log Out",
                    buttonType = ButtonType.Danger
                )

            }
        }
    }
}
