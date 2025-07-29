// ApiCreateBillRequest.kt
package com.example.sharespace.core.data.repository.dto.finance

import com.google.gson.annotations.SerializedName

data class ApiCreateBillRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("amount")
    val amount: String, // Backend expects string that gets converted to int

    @SerializedName("payer_id")
    val payerId: String, // Backend expects string that gets converted to int

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
    val userId: String, // Backend expects string

    @SerializedName("amount_due")
    val amountDue: String // Backend expects string that gets converted to int
)

// ApiCreateBillResponse.kt
data class ApiCreateBillResponse(
    @SerializedName("bill_id")
    val billId: Int,

    @SerializedName("message")
    val message: String
)

// Update your ApiTransaction DTO to handle null values:
data class ApiTransaction(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String?, // Make nullable

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("category")
    val category: String?, // Make nullable

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("payer_user_id")
    val payerUserId: Int,

    @SerializedName("scheduled_date")
    val scheduledDate: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("meta_data")
    val metaData: ApiTransactionMetaData?
)

// ApiTransactionMetaData.kt
data class ApiTransactionMetaData(
    @SerializedName("users")
    val users: List<ApiTransactionUser>?
)

// ApiTransactionUser.kt
data class ApiTransactionUser(
    @SerializedName("user_id")
    val userId: String, // Note: Backend returns this as string in response

    @SerializedName("amount_due")
    val amountDue: Double // Backend returns this as Double in response
)

// ApiTransactionListResponse.kt
// Since the backend returns an array directly, we can use List<ApiTransaction>
// But if you want a wrapper class for consistency:
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
    val amount: Double, // Backend expects double for payments

    @SerializedName("payer_id")
    val payerId: String, // Backend expects string that gets converted to int

    @SerializedName("payee_id")
    val payeeId: String // Backend expects string that gets converted to int
)

data class ApiCreatePaymentResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("payment_id")
    val paymentId: Int? = null
)