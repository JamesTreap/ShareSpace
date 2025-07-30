package com.example.sharespace.core.domain.model

enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    UNKNOWN;

    companion object {
        fun fromString(status: String): InvitationStatus {
            return when (status.uppercase()) {
                "PENDING" -> PENDING
                "ACCEPTED" -> ACCEPTED
                "DECLINED" -> DECLINED
                else -> UNKNOWN
            }
        }
    }
}