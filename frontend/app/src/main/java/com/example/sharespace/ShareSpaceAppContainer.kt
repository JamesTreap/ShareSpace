package com.example.sharespace

import android.content.Context
import com.example.sharespace.auth.data.repository.NetworkAuthRepository
import com.example.sharespace.core.data.local.sessionDataStore
import com.example.sharespace.core.data.remote.ApiClient
import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.RoomRepository
import com.example.sharespace.core.data.repository.UserSessionRepository
import com.example.sharespace.core.data.repository.local.PreferencesUserSessionRepository
import com.example.sharespace.core.data.repository.network.NetworkRoomRepository
import com.example.sharespace.data.repository.AuthRepository

interface ShareSpaceAppContainer {
    val userSessionRepository: UserSessionRepository
    val roomRepository: RoomRepository
    val authRepository: AuthRepository
}

class DefaultShareSpaceAppContainer(applicationContext: Context) : ShareSpaceAppContainer {
    private val apiService: ApiService = ApiClient.apiService

    // --- Repositories ---
    override val userSessionRepository: UserSessionRepository by lazy {
        PreferencesUserSessionRepository(applicationContext.sessionDataStore)
    }

    override val roomRepository: RoomRepository by lazy {
        NetworkRoomRepository(
            apiService = apiService,
        )
    }

    override val authRepository: AuthRepository by lazy {
        NetworkAuthRepository(apiService = apiService)
    }

//    val userRepository: UserRepository = UserRepository(
//        apiService = apiService,
//        tokenStorage = tokenStorage
//    )
//
//    val taskRepository: TaskRepository = TaskRepository(
//        apiService = apiService,
//        tokenStorage = tokenStorage
//    )
//
//    val financeRepository: FinanceRepository = FinanceRepository(
//        apiService = apiService,
//        tokenStorage = tokenStorage
//    )


}