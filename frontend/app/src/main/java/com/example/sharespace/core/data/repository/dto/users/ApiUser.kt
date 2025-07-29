package com.example.sharespace.core.data.repository.dto.users

import com.google.gson.annotations.SerializedName

data class ApiUser(
    val id: Int,
    val name: String,
    val username: String,
    val profilePictureUrl: String?
)


data class ApiUserWithDebts(
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("name")
    val name: String?,

    @SerializedName("email")
    val email: String,

    @SerializedName("profile_picture_url")
    val profilePictureUrl: String?,

    @SerializedName("owes")
    val owes: Map<String, Double>, // What this user owes TO others (user_id -> amount)

    @SerializedName("debts")
    val debts: Map<String, Double> // What others owe TO this user (user_id -> amount)
)


data class ApiRoomMembersWithDebtsResponse(
    val members: List<ApiUserWithDebts>
)

// DebtSummary.kt - Simplified debt summary for UI
data class DebtSummary(
    val userId: Int,
    val userName: String,
    val profilePictureUrl: String?,
    val netBalance: Double, // Positive = they owe you, Negative = you owe them
    val owesAmount: Double, // Total they owe you
    val owedAmount: Double  // Total you owe them
)