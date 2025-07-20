package com.example.sharespace.auth.data.repository

import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.dto.ApiCreateAccountRequest
import com.example.sharespace.core.data.repository.dto.ApiCreateAccountResponse
import com.example.sharespace.core.data.repository.dto.ApiLoginRequest
import com.example.sharespace.core.data.repository.dto.ApiLoginResponse
import com.example.sharespace.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException


class NetworkAuthRepository(
    private val apiService: ApiService
) : AuthRepository {

    override suspend fun login(usernameOrEmail: String, password: String): ApiLoginResponse {
        return withContext(Dispatchers.IO) {
            val apiRequest = ApiLoginRequest(username = usernameOrEmail, password = password)
            val response = apiService.login(apiRequest)

            if (response.isSuccessful) {
                response.body()
                    ?: throw IllegalStateException("API response body was null for login")
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun createAccount(
        username: String,
        email: String,
        password: String
    ): ApiCreateAccountResponse {
        return withContext(Dispatchers.IO) {
            val apiRequest =
                ApiCreateAccountRequest(username = username, email = email, password = password)
            val response = apiService.createAccount(apiRequest)

            if (response.isSuccessful) {
                response.body()
                    ?: throw IllegalStateException("API response body was null for create account")
            } else {
                throw HttpException(response)
            }
        }
    }
}
