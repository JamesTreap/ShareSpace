package com.example.sharespace.client_caching.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DogImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: DogImage)

    @Query("SELECT * FROM dog_image WHERE id = 0 LIMIT 1")
    suspend fun getCachedImage(): DogImage?
}
