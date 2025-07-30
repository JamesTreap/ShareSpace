// ApiCreateBillRequest.kt
package com.example.sharespace.core.data.repository.dto.finance

import com.google.gson.annotations.SerializedName

data class ApiCreateBillRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("amount")
    val amount: String,

    @SerializedName("payer_id")
    val payerId: String,

    @SerializedName("users")
    val users: List<ApiBillUser>,

    @SerializedName("frequency")
    val frequency: String,

    @SerializedName("repeat")
    val repeat: String
)

// ApiBillUser.kt
data class ApiBillUser(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("amount_due")
    val amountDue: String
)

// ApiCreateBillResponse.kt
data class ApiCreateBillResponse(
    @SerializedName("bill_id")
    val billId: Int,

    @SerializedName("message")
    val message: String
)

data class ApiTransactionMetaData(
    @SerializedName("users")
    val users: List<ApiTransactionUser>?
)

data class ApiTransactionUser(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("amount_due")
    val amountDue: Double
)

data class ApiTransactionListResponse(
    val transactions: List<ApiTransaction>
)

data class ApiDeleteResponse(
    val message: String
)

data class ApiCreatePaymentRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("payer_id")
    val payerId: String,

    @SerializedName("payee_id")
    val payeeId: String
)

data class ApiCreatePaymentResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("payment_id")
    val paymentId: Int? = null
)