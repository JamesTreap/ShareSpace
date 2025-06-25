package com.example.sharespace.client_caching.repository

import com.example.sharespace.client_caching.data.DogImage
import com.example.sharespace.client_caching.data.DogImageDao
import com.example.sharespace.client_caching.network.DogApiClient

class DogImageRepository(private val dao: DogImageDao) {

    // Load from DB (cache)
    suspend fun getCachedImage(): DogImage? {
        return dao.getCachedImage()
    }

    // Fetch new image from API and store it in Room
    suspend fun fetchAndCacheImage(): DogImage {
        val newUrl = DogApiClient.fetchDogImage()
        val cached = DogImage(imageUrl = newUrl)
        dao.insertImage(cached)
        return cached
    }
}
