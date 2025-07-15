package com.example.sharespace

import android.content.Context
import com.example.sharespace.core.data.local.TokenStorage
import com.example.sharespace.core.data.remote.ApiClient
import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.RoomRepository
import com.example.sharespace.core.data.repository.implementation.RoomRepositoryImpl

interface ShareSpaceAppContainer {
    val roomRepository: RoomRepository
}

class DefaultShareSpaceAppContainer(applicationContext: Context) : ShareSpaceAppContainer {
    private val apiService: ApiService = ApiClient.apiService

    // --- Local Storage for Authentication ---
    // TokenStorage needs application context for DataStore/SharedPreferences
    val tokenStorage = TokenStorage

    // --- Repositories ---
    // Repositories are instantiated here, injecting their dependencies (ApiService, TokenStorage)

//    val authRepository: AuthRepository = AuthRepository(
//        apiService = apiService,
//        tokenStorage = tokenStorage
//    )

    override val roomRepository: RoomRepository = RoomRepositoryImpl(
        apiService = apiService,
    )

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