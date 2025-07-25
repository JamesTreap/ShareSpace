package com.example.sharespace.core.domain.model

import com.example.sharespace.core.data.repository.dto.users.ApiUser

data class User(
    val id: Int,
    val name: String,
    val photoUrl: String? = null,
    val username: String
) {
    // Secondary constructor that takes an ApiUser
    constructor(apiUser: ApiUser) : this(
        id = apiUser.id,
        name = apiUser.name,
        photoUrl = apiUser.profilePictureUrl, // Map profilePictureUrl to photoUrl
        username = apiUser.username
    )
}