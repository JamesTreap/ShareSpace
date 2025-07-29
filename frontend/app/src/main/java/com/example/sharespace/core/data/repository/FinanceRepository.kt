// Create this file: com/example/sharespace/core/data/repository/FinanceRepository.kt
package com.example.sharespace.core.data.repository

import com.example.sharespace.core.data.repository.dto.finance.ApiCreateBillRequest
import com.example.sharespace.core.data.repository.dto.finance.ApiCreateBillResponse
import com.example.sharespace.core.data.repository.dto.finance.ApiCreatePaymentRequest
import com.example.sharespace.core.data.repository.dto.finance.ApiCreatePaymentResponse
import com.example.sharespace.core.data.repository.dto.finance.ApiTransaction

interface FinanceRepository {
    suspend fun getTransactionList(token: String, roomId: Int): List<ApiTransaction>

    suspend fun createBill(
        token: String,
        roomId: Int,
        request: ApiCreateBillRequest
    ): ApiCreateBillResponse

    suspend fun deleteBill(token: String, billId: Int): String

    suspend fun deletePayment(token: String, paymentId: Int): String

    suspend fun createPayment(
        token: String,
        roomId: Int,
        request: ApiCreatePaymentRequest
    ): ApiCreatePaymentResponse

}