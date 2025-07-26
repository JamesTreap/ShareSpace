package com.example.sharespace.core.data.repository

import kotlinx.coroutines.flow.Flow

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
     * A flow that emits the ID of the currently logged-in user.
     * Emits null if no user ID is stored or if it's cleared.
     * The ID corresponds to the 'id' field from ApiUser.
     */
    val currentUserIdFlow: Flow<Int?> // Changed to Int

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
     * Saves the ID of the current user.
     * @param userId The ID of the user to save. Pass null to clear the current user ID.
     */
    suspend fun saveCurrentUserId(userId: Int?)

    /**
     * Clears the stored user authentication token.
     */
    suspend fun clearUserToken()

    /**
     * Clears the stored active room ID.
     */
    suspend fun clearActiveRoomId()

    /**
     * Clears the stored current user ID.
     */
    suspend fun clearCurrentUserId()

    /**
     * Clears all stored session data (token, active room ID, and current user ID).
     */
    suspend fun clearAllSessionData()
}
