package com.example.sharespace.core.data.repository.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.sharespace.core.data.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class PreferencesUserSessionRepository(private val dataStore: DataStore<Preferences>) :
    UserSessionRepository {

    companion object SessionKeys {
        val USER_TOKEN = stringPreferencesKey("user_token")
        val ACTIVE_ROOM_ID = intPreferencesKey("active_room_id")
        val CURRENT_USER_ID = intPreferencesKey("current_user_id") // New key for User ID
    }

    override val userTokenFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_TOKEN]
        }

    override val activeRoomIdFlow: Flow<Int?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[ACTIVE_ROOM_ID]
        }

    // New Flow for Current User ID
    override val currentUserIdFlow: Flow<Int?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[CURRENT_USER_ID]
        }

    override suspend fun saveUserToken(token: String) {
        dataStore.edit { preferences ->
            preferences[USER_TOKEN] = token
        }
    }

    override suspend fun saveActiveRoomId(roomId: Int?) {
        dataStore.edit { preferences ->
            if (roomId != null) {
                preferences[ACTIVE_ROOM_ID] = roomId
            } else {
                preferences.remove(ACTIVE_ROOM_ID)
            }
        }
    }

    // New function to save Current User ID
    override suspend fun saveCurrentUserId(userId: Int?) {
        dataStore.edit { preferences ->
            if (userId != null) {
                preferences[CURRENT_USER_ID] = userId
            } else {
                preferences.remove(CURRENT_USER_ID)
            }
        }
    }

    override suspend fun clearUserToken() {
        dataStore.edit { preferences ->
            preferences.remove(USER_TOKEN)
        }
    }

    override suspend fun clearActiveRoomId() {
        dataStore.edit { preferences ->
            preferences.remove(ACTIVE_ROOM_ID)
        }
    }

    // New function to clear Current User ID
    override suspend fun clearCurrentUserId() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_ID)
        }
    }

    override suspend fun clearAllSessionData() {
        dataStore.edit { preferences ->
            preferences.clear() // This will clear token, room ID, and user ID
        }
    }
}