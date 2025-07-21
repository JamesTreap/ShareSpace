package com.example.sharespace.core.domain.model


/**
 * Represents the possible statuses of a room invitation.
 */
enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    UNKNOWN; // For statuses from the API that don't match known ones

    companion object {
        fun fromString(status: String): InvitationStatus {
            return when (status.uppercase()) { // Convert to uppercase for case-insensitive matching
                "PENDING" -> PENDING
                "ACCEPTED" -> ACCEPTED
                "DECLINED" -> DECLINED
                else -> UNKNOWN
            }
        }
    }
}