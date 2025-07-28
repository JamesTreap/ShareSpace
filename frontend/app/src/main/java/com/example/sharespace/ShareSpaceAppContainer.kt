package com.example.sharespace

import android.content.Context
import com.example.sharespace.auth.data.repository.NetworkAuthRepository
import com.example.sharespace.calendar.data.repository.NetworkCalendarRepository
import com.example.sharespace.core.data.local.sessionDataStore
import com.example.sharespace.core.data.remote.ApiClient
import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.CalendarRepository
import com.example.sharespace.core.data.repository.RoomRepository
import com.example.sharespace.core.data.repository.TaskRepository
import com.example.sharespace.core.data.repository.UserSessionRepository
import com.example.sharespace.core.data.repository.local.PreferencesUserSessionRepository
import com.example.sharespace.core.data.repository.network.NetworkRoomRepository
import com.example.sharespace.core.data.repository.network.NetworkTaskRepository
import com.example.sharespace.data.repository.AuthRepository
import com.example.sharespace.user.data.repository.ProfileRepository

interface ShareSpaceAppContainer {
    val userSessionRepository: UserSessionRepository
    val roomRepository: RoomRepository
    val authRepository: AuthRepository
    val profileRepository: ProfileRepository
    val taskRepository: TaskRepository
    val calendarRepository: CalendarRepository
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

    override val profileRepository: ProfileRepository by lazy {
        ProfileRepository(api = apiService)
    }

    override val taskRepository: TaskRepository by lazy {
        NetworkTaskRepository(
            apiService = apiService
        )
    }

    override val calendarRepository: CalendarRepository by lazy {
        NetworkCalendarRepository(apiService = apiService)
    }

//    val userRepository: UserRepository = UserRepository(
//        apiService = apiService,
//        tokenStorage = tokenStorage
//    )
//

//
//    val financeRepository: FinanceRepository = FinanceRepository(
//        apiService = apiService,
//        tokenStorage = tokenStorage
//    )


}