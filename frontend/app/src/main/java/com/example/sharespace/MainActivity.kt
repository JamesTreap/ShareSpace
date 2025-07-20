package com.example.sharespace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sharespace.core.ui.theme.ShareSpaceTheme

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen().apply {
            setKeepOnScreenCondition {
                mainViewModel.initialDestinationState.value is InitialDestinationState.Loading
            }
        }
        enableEdgeToEdge()

        setContent {
            ShareSpaceTheme {
                val currentInitialState by mainViewModel.initialDestinationState.collectAsStateWithLifecycle()
                when (val state = currentInitialState) {
                    is InitialDestinationState.Loading -> {
                    }

                    is InitialDestinationState.Loaded -> {
                        ShareSpaceApp(startDestination = state.startDestinationRoute)
                    }
                }
            }
        }
    }
}