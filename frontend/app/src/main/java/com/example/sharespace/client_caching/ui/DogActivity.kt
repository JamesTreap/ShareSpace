package com.example.sharespace.client_caching.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharespace.client_caching.data.DogImageDatabase
import com.example.sharespace.client_caching.repository.DogImageRepository

class DogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up dependencies manually
        val dao = DogImageDatabase.getInstance(applicationContext).dogImageDao()
        val repository = DogImageRepository(dao)
        val viewModel = DogImageViewModel(repository)

        setContent {
            DogImageScreen(viewModel = viewModel)
        }
    }
}
