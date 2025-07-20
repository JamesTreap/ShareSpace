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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.core.data.local.TokenStorage
import com.example.sharespace.core.ui.theme.TextSecondary
import com.example.sharespace.user.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class User(
    val id: String,
    val name: String,
    val username: String,
    val profilePictureUrl: String?
)

class ProfileScreenViewModel : ViewModel() {
    // backing state
    private val _user = mutableStateOf<User?>(null)

    // public streams
    val user: MutableState<User?> = _user


    private val repository = ProfileRepository()

    fun loadData(token: String) {

        viewModelScope.launch {
            try {
                val apiUser = repository.getUser(token)


                _user.value = User(
                    id = apiUser.id.toString(),
                    name = apiUser.name,
                    username = apiUser.username,
                    profilePictureUrl = apiUser.profilePictureUrl
                )

            } catch (e: Exception) {
                e.printStackTrace()
                println("Error loading profile data: ${e.message}")
            }
        }
    }
}


@Composable
fun ViewProfileScreen(
    viewModel: ProfileScreenViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {

    val user by viewModel.user
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenState = produceState<String?>(initialValue = null) {
        value = TokenStorage.getToken(context)
    }
    val token = tokenState.value

    LaunchedEffect(token) {
        if (token != null) {
            println("Calling loadData with token: $token")
            viewModel.loadData(token)
        } else {
            println("Token is null, not calling loadData")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(vertical = 24.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize() ) {
            ScreenHeader(
                title = user?.name ?: "View Profile",
                onBackClick = onNavigateBack,
                actions = {},
                photoUrl = user?.profilePictureUrl
            )

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.8f), thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 18.dp))

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
                    Text("User ID", style = MaterialTheme.typography.bodyMedium,  color = TextSecondary)
                    Text(user?.id ?: "", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Username", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(user?.username ?: "", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Profile")
                }

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Log Out")
                }
            }
        }
    }
}
