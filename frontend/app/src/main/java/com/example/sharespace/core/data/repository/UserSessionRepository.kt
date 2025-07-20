package com.example.sharespace.core.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing user session data stored in DataStore Preferences.
 */
interface UserSessionRepository {

    /**
     * A flow that emits the current user's authentication token.
     * Emits null if no token is stored or if it's cleared.
     */
    val userTokenFlow: Flow<String?>

    /**
     * A flow that emits the ID of the currently active room for the user.
     * Emits null if no room is active or if it's cleared.
     */
    val activeRoomIdFlow: Flow<Int?>

    /**
     * Saves the user's authentication token.
     * @param token The token to save.
     */
    suspend fun saveUserToken(token: String)

    /**
     * Saves the ID of the active room.
     * @param roomId The ID of the room to save. Pass null to clear the active room ID.
     */
    suspend fun saveActiveRoomId(roomId: Int?)

    /**
     * Clears the stored user authentication token.
     */
    suspend fun clearUserToken()

    /**
     * Clears the stored active room ID.
     */
    suspend fun clearActiveRoomId()

    /**
     * Clears all stored session data (token and active room ID).
     */
    suspend fun clearAllSessionData()
}
