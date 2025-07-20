package com.example.sharespace.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
private const val USER_SESSION_PREFERENCES_NAME = "user_session_prefs"

internal val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_SESSION_PREFERENCES_NAME
)
