package com.example.sharespace.client_caching.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.example.sharespace.client_caching.network.model.DogApiResponse

object DogApiClient {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchDogImage(): String {
        val response: DogApiResponse = client.get("https://dog.ceo/api/breeds/image/random").body()
        return response.message
    }
}
