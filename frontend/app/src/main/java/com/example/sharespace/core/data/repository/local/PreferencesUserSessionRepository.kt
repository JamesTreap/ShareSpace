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
        val ACTIVE_ROOM_ID = intPreferencesKey("active_room_id") // Use intPreferencesKey for Int
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
            preferences[SessionKeys.USER_TOKEN]
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
            preferences[SessionKeys.ACTIVE_ROOM_ID]
        }

    override suspend fun saveUserToken(token: String) {
        dataStore.edit { preferences ->
            preferences[SessionKeys.USER_TOKEN] = token
        }
    }

    override suspend fun saveActiveRoomId(roomId: Int?) {
        dataStore.edit { preferences ->
            if (roomId != null) {
                preferences[SessionKeys.ACTIVE_ROOM_ID] = roomId
            } else {
                preferences.remove(SessionKeys.ACTIVE_ROOM_ID)
            }
        }
    }

    override suspend fun clearUserToken() {
        dataStore.edit { preferences ->
            preferences.remove(SessionKeys.USER_TOKEN)
        }
    }

    override suspend fun clearActiveRoomId() {
        dataStore.edit { preferences ->
            preferences.remove(SessionKeys.ACTIVE_ROOM_ID)
        }
    }

    override suspend fun clearAllSessionData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
