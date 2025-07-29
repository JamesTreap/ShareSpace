package com.example.sharespace.core.domain.model

import com.example.sharespace.core.data.repository.dto.finance.ApiBill
data class Bill(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val createdAt: String,
    val deadline: String?,
    val payerUserId: Int,
    val scheduledDate: String,
    val type: String,
    val metadata: BillMetadata?
) {
    constructor(apiBill: ApiBill) : this(
        id = apiBill.id,
        title = apiBill.title,
        amount = apiBill.amount,
        category = apiBill.category,
        createdAt = apiBill.createdAt,
        deadline = apiBill.deadline,
        payerUserId = apiBill.payerUserId,
        scheduledDate = apiBill.scheduledDate,
        type = apiBill.type,
        metadata = apiBill.metadata?.let {
            BillMetadata(
                users = it.users.map { user ->
                    UserDueAmount(
                        userId = user.user_id,
                        amountDue = user.amount_due
                    )
                }
            )
        }
    )
}

data class BillMetadata(
    val users: List<UserDueAmount>
)

data class UserDueAmount(
    val userId: String,
    val amountDue: Double
)