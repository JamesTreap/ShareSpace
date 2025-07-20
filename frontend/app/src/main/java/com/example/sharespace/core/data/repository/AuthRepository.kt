package com.example.sharespace.data.repository

import com.example.sharespace.core.data.repository.dto.ApiCreateAccountResponse
import com.example.sharespace.core.data.repository.dto.ApiLoginResponse

interface AuthRepository {
    /**
     * Attempts to log in a user.
     * @return LoginApiResponse if successful and body is not null.
     * @throws IllegalStateException if HTTP call is successful (2xx) but the response body is null.
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues.
     */
    suspend fun login(usernameOrEmail: String, password: String): ApiLoginResponse

    /**
     * Attempts to create a new user account.
     * @return CreateAccountApiResponse if successful and body is not null.
     * @throws IllegalStateException if HTTP call is successful (2xx) but the response body is null.
     * @throws retrofit2.HttpException if the server returns a non-2xx HTTP status.
     * @throws java.io.IOException for network issues.
     */
    suspend fun createAccount(
        username: String,
        email: String,
        password: String
    ): ApiCreateAccountResponse
}