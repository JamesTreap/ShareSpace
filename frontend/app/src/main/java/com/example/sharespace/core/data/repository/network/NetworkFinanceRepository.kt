package com.example.sharespace.core.data.repository.network

import com.example.sharespace.core.data.remote.ApiService
import com.example.sharespace.core.data.repository.FinanceRepository
import com.example.sharespace.core.data.repository.dto.finance.ApiBill
import com.example.sharespace.core.data.repository.dto.finance.ApiCreateBillRequest
import com.example.sharespace.core.data.repository.dto.finance.ApiCreateBillResponse
import com.example.sharespace.core.data.repository.dto.finance.ApiCreatePaymentRequest
import com.example.sharespace.core.data.repository.dto.finance.ApiCreatePaymentResponse
import com.example.sharespace.core.data.repository.dto.finance.ApiTransaction
import retrofit2.HttpException


class NetworkFinanceRepository(private val apiService: ApiService) : FinanceRepository {

    //    override suspend fun getTransactionList(token: String, roomId: Int): List<ApiTransaction> {
//        val response = apiService.getTransactionList(roomId = roomId, token = "Bearer $token")
//        if (response.isSuccessful) {
//            return response.body() ?: emptyList()
//        } else {
//            throw HttpException(response)
//        }
//    }
    override suspend fun getTransactionList(token: String, roomId: Int): List<ApiTransaction> {
        val response = apiService.getTransactionList(roomId = roomId, token = "Bearer $token")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun getBillList(token: String, roomId: Int): List<ApiBill> {
        val transactions = getTransactionList(token, roomId)
        return transactions.filterIsInstance<ApiBill>()
    }

    override suspend fun createBill(
        token: String, roomId: Int, request: ApiCreateBillRequest
    ): ApiCreateBillResponse {
        val response = apiService.createBill(
            roomId = roomId, token = "Bearer $token", request = request
        )
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for createBill")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun deleteBill(token: String, billId: Int): String {
        val response = apiService.deleteBill(
            billId = billId, authorization = "Bearer $token"
        )
        if (response.isSuccessful) {
            return response.body()?.message
                ?: throw IllegalStateException("API response body was null for deleteBill")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun deletePayment(token: String, paymentId: Int): String {
        val response = apiService.deletePayment(
            paymentId = paymentId, authorization = "Bearer $token"
        )
        if (response.isSuccessful) {
            return response.body()?.message
                ?: throw IllegalStateException("API response body was null for deletePayment")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun createPayment(
        token: String, roomId: Int, request: ApiCreatePaymentRequest
    ): ApiCreatePaymentResponse {
        val response = apiService.createPayment(
            roomId = roomId, authorization = "Bearer $token", request = request
        )
        if (response.isSuccessful) {
            return response.body()
                ?: throw IllegalStateException("API response body was null for createPayment")
        } else {
            throw HttpException(response)
        }
    }
}