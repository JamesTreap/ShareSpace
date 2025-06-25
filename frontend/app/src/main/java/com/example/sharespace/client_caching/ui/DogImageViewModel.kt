package com.example.sharespace.client_caching.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.sharespace.client_caching.data.DogImage
import com.example.sharespace.client_caching.repository.DogImageRepository

class DogImageViewModel(
    private val repository: DogImageRepository
) : ViewModel() {

    private val _dogImage = MutableStateFlow<DogImage?>(null)
    val dogImage: StateFlow<DogImage?> = _dogImage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadImage(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = if (forceRefresh) {
                repository.fetchAndCacheImage()
            } else {
                repository.getCachedImage() ?: repository.fetchAndCacheImage()
            }

            _dogImage.value = result
            _isLoading.value = false
        }
    }
}
