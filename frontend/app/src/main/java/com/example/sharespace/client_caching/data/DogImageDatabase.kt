package com.example.sharespace.client_caching.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DogImage::class], version = 1)
abstract class DogImageDatabase : RoomDatabase() {

    abstract fun dogImageDao(): DogImageDao

    companion object {
        @Volatile
        private var INSTANCE: DogImageDatabase? = null

        fun getInstance(context: Context): DogImageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DogImageDatabase::class.java,
                    "dog_image_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
