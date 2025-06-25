package com.example.sharespace.client_caching.network.model

import kotlinx.serialization.Serializable

@Serializable
data class DogApiResponse(
    val message: String,
    val status: String
)
