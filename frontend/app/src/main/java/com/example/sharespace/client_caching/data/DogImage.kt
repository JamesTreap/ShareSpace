package com.example.sharespace.client_caching.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dog_image")
data class DogImage(
    @PrimaryKey val id: Int = 0,
    val imageUrl: String
)
