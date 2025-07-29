package com.example.sharespace.ui.screens.finance

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sharespace.ShareSpaceApplication
import com.example.sharespace.core.data.repository.FinanceRepository
import com.example.sharespace.core.data.repository.RoomRepository
import com.example.sharespace.core.data.repository.UserRepository
import com.example.sharespace.core.data.repository.UserSessionRepository
import com.example.sharespace.core.data.repository.dto.finance.ApiBill
import com.example.sharespace.core.data.repository.dto.finance.ApiCreateBillRequest
import com.example.sharespace.core.data.repository.dto.finance.ApiBillUser
import com.example.sharespace.core.data.repository.dto.finance.ApiCreatePaymentRequest
import com.example.sharespace.core.data.repository.dto.finance.ApiTransaction
import com.example.sharespace.core.data.repository.dto.users.ApiUser
import com.example.sharespace.core.data.repository.dto.users.ApiUserWithDebts
import com.example.sharespace.core.data.repository.dto.users.DebtSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

// ADD THIS DATA CLASS HERE (from AddBillViewModel)
data class RoommateSplit(
    val user: ApiUser,
    val amount: String = "0",
    val currentBalance: Double = 0.0
)

class FinanceManagerViewModel(
    private val userSessionRepository: UserSessionRepository,
    private val financeRepository: FinanceRepository,
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // State for transactions
    private val _transactions = MutableStateFlow<List<ApiBill>>(emptyList())
    val transactions: StateFlow<List<ApiBill>> = _transactions

    // State for roommates
    private val _roommates = MutableStateFlow<List<ApiUser>>(emptyList())
    val roommates: StateFlow<List<ApiUser>> = _roommates

    // Loading state
    private val _isLoading = mutableStateOf(false)
    val isLoading = _isLoading

    // Error state
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage = _errorMessage

    // New state for debt management
    private val _roomMembersWithDebts = MutableStateFlow<List<ApiUserWithDebts>>(emptyList())
    val roomMembersWithDebts: StateFlow<List<ApiUserWithDebts>> = _roomMembersWithDebts

    private val _debtSummaries = MutableStateFlow<List<DebtSummary>>(emptyList())
    val debtSummaries: StateFlow<List<DebtSummary>> = _debtSummaries
    private val _addBillRoommates = MutableStateFlow<List<RoommateSplit>>(emptyList())
    val addBillRoommates: StateFlow<List<RoommateSplit>> = _addBillRoommates

    private val _billCreated = mutableStateOf(false)
    val billCreated = _billCreated

//    fun loadTransactions() {
//        viewModelScope.launch {
//            try {
//                _isLoading.value = true
//                _errorMessage.value = null
//
//                val token = userSessionRepository.userTokenFlow.first()
//                val activeRoomId = userSessionRepository.activeRoomIdFlow.first()
//
//                if (token == null || activeRoomId == null) {
//                    _errorMessage.value = "Authentication or room information is missing."
//                    return@launch
//                }
//
//                val transactionList = financeRepository.getTransactionList(token, activeRoomId)
//                val roommatesList = roomRepository.getRoomMembers(token, activeRoomId)
//
//                _transactions.value = transactionList
//                _roommates.value = roommatesList
//
////                Log.d(TAG, "✅ Loaded ${transactionList.size} transactions and ${roommatesList.size} roommates")
//
//                loadRoomMembersWithDebtsInternal(token, activeRoomId)
//
//            } catch (e: Exception) {
//                Log.e(TAG, "❌ Error loading data: ${e.message}", e)
//                _errorMessage.value = "Failed to load data: ${e.message}"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }

    fun loadTransactions() {
        viewModelScope.launch {
            Log.d(TAG, "loadTransactions called.") // Log when the function starts
            try {
                _isLoading.value = true
                _errorMessage.value = null
                Log.d(TAG, "isLoading set to true, errorMessage cleared.")

                val token = userSessionRepository.userTokenFlow.first()
                val activeRoomId = userSessionRepository.activeRoomIdFlow.first()

                if (token == null || activeRoomId == null) {
                    _errorMessage.value = "Authentication or room information is missing."
                    Log.w(TAG, "Token or activeRoomId is null. Token present: ${token != null}, RoomId present: ${activeRoomId != null}. Aborting.")
                    return@launch
                }
                Log.d(TAG, "Token and activeRoomId retrieved. RoomId: $activeRoomId.")


                Log.d(TAG, "Fetching transaction list...")
                val transactionList = financeRepository.getBillList(token, activeRoomId)
                Log.i(TAG, "Fetched ${transactionList.size} transactions.")


                _transactions.value = transactionList

                Log.d(TAG, "Fetching roommates list...")
                val roommatesList = roomRepository.getRoomMembers(token, activeRoomId)
                Log.i(TAG, "Fetched ${roommatesList.size} roommates.")
                // You could also log roommates here if needed, similar to transactions
                _roommates.value = roommatesList

                Log.d(TAG, "✅ Successfully loaded ${transactionList.size} transactions and ${roommatesList.size} roommates.")

                Log.d(TAG, "Calling loadRoomMembersWithDebtsInternal...")
                loadRoomMembersWithDebtsInternal(token, activeRoomId)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in loadTransactions: ${e.message}", e)
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "isLoading set to false. loadTransactions finished.")
            }
        }
    }

    private suspend fun loadRoomMembersWithDebtsInternal(token: String, activeRoomId: Int) {
        try {
            val currentUserId = userSessionRepository.currentUserIdFlow.first() ?: return
            val membersWithDebts = userRepository.getRoomMembersWithDebts(token, activeRoomId)
            _roomMembersWithDebts.value = membersWithDebts

            val summaries = calculateDebtSummaries(membersWithDebts, currentUserId)
            _debtSummaries.value = summaries

//            Log.d(TAG, "✅ Loaded debt information.")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading debt data: ${e.message}", e)
        }
    }

    fun loadRoomMembersWithDebts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = userSessionRepository.userTokenFlow.first()
                val activeRoomId = userSessionRepository.activeRoomIdFlow.first()
                if (token != null && activeRoomId != null) {
                    loadRoomMembersWithDebtsInternal(token, activeRoomId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading room members with debts: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun loadRoommatesForBill() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val token = userSessionRepository.userTokenFlow.first()
                val activeRoomId = userSessionRepository.activeRoomIdFlow.first()
                val currentUserId = userSessionRepository.currentUserIdFlow.first()

                if (token == null || activeRoomId == null || currentUserId == null) {
                    _errorMessage.value = "Missing authentication or room information"
                    return@launch
                }

                val roommatesList = roomRepository.getRoomMembers(token, activeRoomId)
                val membersWithDebts = userRepository.getRoomMembersWithDebts(token, activeRoomId)
                val currentUserDebts = membersWithDebts.find { it.id == currentUserId }

                _addBillRoommates.value = roommatesList.map { user ->
                    val currentBalance = calculateCurrentBalance(currentUserId, user.id, currentUserDebts)
                    RoommateSplit(user = user, currentBalance = currentBalance)
                }
//                Log.d(TAG, "✅ Loaded ${roommatesList.size} roommates for bill creation.")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading roommates: ${e.message}", e)
                _errorMessage.value = "Failed to load roommates: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateCurrentBalance(currentUserId: Int, roommateId: Int, currentUserDebts: ApiUserWithDebts?): Double {
        if (currentUserDebts == null || currentUserId == roommateId) return 0.0
        val roommateIdStr = roommateId.toString()
        val owesToRoommate = currentUserDebts.owes[roommateIdStr] ?: 0.0
        val roommateOwesUser = currentUserDebts.debts[roommateIdStr] ?: 0.0
        return roommateOwesUser - owesToRoommate
    }

    fun updateRoommateAmount(userId: Int, amount: String) {
        _addBillRoommates.value = _addBillRoommates.value.map {
            if (it.user.id == userId) it.copy(amount = amount) else it
        }
    }

    fun splitEvenly(totalAmount: String) {
        val total = totalAmount.toDoubleOrNull() ?: 0.0
        if (_addBillRoommates.value.isNotEmpty() && total > 0) {
            val splitAmount = String.format("%.2f", total / _addBillRoommates.value.size)
            _addBillRoommates.value = _addBillRoommates.value.map { it.copy(amount = splitAmount) }
        }
    }

    fun createBill(
        title: String,
        category: String,
        totalAmount: String,
        frequency: String,
        repeats: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val token = userSessionRepository.userTokenFlow.first()
                val activeRoomId = userSessionRepository.activeRoomIdFlow.first()
                val currentUserId = userSessionRepository.currentUserIdFlow.first()

                if (token == null || activeRoomId == null || currentUserId == null) {
                    _errorMessage.value = "Missing authentication or room information"
                    return@launch
                }

                if (title.isBlank()) {
                    _errorMessage.value = "Title is required"
                    return@launch
                }

                val amount = totalAmount.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    _errorMessage.value = "Please enter a valid amount"
                    return@launch
                }

                val usersWithAmounts = _addBillRoommates.value.mapNotNull { split ->
                    val userAmount = split.amount.toDoubleOrNull()
                    if (userAmount != null && userAmount > 0) {
                        ApiBillUser(
                            userId = split.user.id.toString(),
                            amountDue = userAmount.toString()
                        )
                    } else null
                }

                if (usersWithAmounts.isEmpty()) {
                    _errorMessage.value = "Please assign amounts to at least one roommate"
                    return@launch
                }

                val sumOfAmounts = usersWithAmounts.sumOf { it.amountDue.toDouble() }
                if (abs(sumOfAmounts - amount) > 0.01) {
                    _errorMessage.value = "Total amount doesn't match individual splits"
                    return@launch
                }

                val request = ApiCreateBillRequest(
                    title = title,
                    category = category,
                    amount = amount.toString(),
                    payerId = currentUserId.toString(),
                    users = usersWithAmounts,
                    frequency = frequency,
                    repeat = repeats
                )

                val response = financeRepository.createBill(token, activeRoomId, request)

                // ** THE IMPORTANT PART **
                // Now we call loadTransactions() AND set the navigation flag
                loadTransactions()
                _billCreated.value = true

                Log.d(TAG, "✅ Bill created successfully: ${response.message}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error creating bill: ${e.message}", e)
                _errorMessage.value = "Failed to create bill: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetBillCreated() {
        _billCreated.value = false
    }

    fun deleteTransaction(transactionId: Int, transactionType: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val token = userSessionRepository.userTokenFlow.first()
                if (token == null) {
                    _errorMessage.value = "Authentication required"
                    return@launch
                }

                // Call the appropriate delete endpoint based on transaction type
                when (transactionType.lowercase()) {
                    "bill" -> {
                        financeRepository.deleteBill(token, transactionId)
                        Log.d(TAG, "✅ Bill deleted successfully: $transactionId")
                    }
                    "payment" -> {
                        financeRepository.deletePayment(token, transactionId)
                        Log.d(TAG, "✅ Payment deleted successfully: $transactionId")
                    }
                    else -> {
                        _errorMessage.value = "Unknown transaction type: $transactionType"
                        return@launch
                    }
                }

                // Reload transactions to update the UI with recalculated values
                loadTransactions()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting transaction: ${e.message}", e)
                _errorMessage.value = "Failed to delete transaction: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun clearError() {
        _errorMessage.value = null
    }
    private fun calculateDebtSummaries(membersWithDebts: List<ApiUserWithDebts>, currentUserId: Int): List<DebtSummary> {
        val summaries = mutableListOf<DebtSummary>()

        // Find current user's debt data
        val currentUserData = membersWithDebts.find { it.id == currentUserId }
        if (currentUserData == null) {
            Log.w(TAG, "Current user not found in members list")
            return emptyList()
        }

        Log.d(TAG, "🔍 CALCULATE_DEBT_SUMMARIES: Current user ID: $currentUserId")
        Log.d(TAG, "🔍 CALCULATE_DEBT_SUMMARIES: Current user owes: ${currentUserData.owes}")
        Log.d(TAG, "🔍 CALCULATE_DEBT_SUMMARIES: Current user debts: ${currentUserData.debts}")

        // Calculate debt summaries with other users
        membersWithDebts.forEach { member ->
            if (member.id != currentUserId) {
                val memberIdStr = member.id.toString()
                val owesAmount = currentUserData.owes[memberIdStr] ?: 0.0  // What current user owes TO this member
                val owedAmount = currentUserData.debts[memberIdStr] ?: 0.0 // What this member owes TO current user
                val netBalance = owedAmount - owesAmount // Positive = they owe you, Negative = you owe them

                Log.d(TAG, "🔍 CALCULATE_DEBT_SUMMARIES: Member ${member.name} (ID: ${member.id})")
                Log.d(TAG, "🔍 CALCULATE_DEBT_SUMMARIES: - Current user owes them: $owesAmount")
                Log.d(TAG, "🔍 CALCULATE_DEBT_SUMMARIES: - They owe current user: $owedAmount")
                Log.d(TAG, "🔍 CALCULATE_DEBT_SUMMARIES: - Net balance: $netBalance")

                // Only include users with non-zero balances
                if (netBalance != 0.0) {
                    summaries.add(
                        DebtSummary(
                            userId = member.id,
                            userName = member.name ?: member.username,
                            profilePictureUrl = member.profilePictureUrl,
                            netBalance = netBalance,
                            owesAmount = owedAmount,  // What they owe you
                            owedAmount = owesAmount   // What you owe them
                        )
                    )
                    Log.d(TAG, "🔍 CALCULATE_DEBT_SUMMARIES: Added ${member.name} to summaries")
                } else {
                    Log.d(TAG, "🔍 CALCULATE_DEBT_SUMMARIES: Skipped ${member.name} (zero balance)")
                }
            }
        }

        Log.d(TAG, "🔍 CALCULATE_DEBT_SUMMARIES: Final summaries count: ${summaries.size}")
        return summaries.sortedByDescending { it.netBalance }
    }

    fun createPayment(
        payeeId: Int,
        amount: String,
        description: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val token = userSessionRepository.userTokenFlow.first()
                val activeRoomId = userSessionRepository.activeRoomIdFlow.first()
                val currentUserId = userSessionRepository.currentUserIdFlow.first()

                if (token == null || activeRoomId == null || currentUserId == null) {
                    _errorMessage.value = "Missing authentication or room information"
                    return@launch
                }

                val paymentAmount = amount.toDoubleOrNull()
                if (paymentAmount == null || paymentAmount <= 0) {
                    _errorMessage.value = "Please enter a valid amount"
                    return@launch
                }

                val request = ApiCreatePaymentRequest(
                    title = description,
                    category = "Payment",
                    amount = paymentAmount,
                    payerId = currentUserId.toString(),
                    payeeId = payeeId.toString()
                )

                val response = financeRepository.createPayment(token, activeRoomId, request)

                // Reload transactions to update balances
                loadTransactions()

                Log.d(TAG, "✅ Payment created successfully: ${response.message}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error creating payment: ${e.message}", e)
                _errorMessage.value = "Failed to create payment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)
                val userSessionRepository = application.container.userSessionRepository
                val financeRepository = application.container.financeRepository
                val roomRepository = application.container.roomRepository
                val userRepository = application.container.userRepository
                FinanceManagerViewModel(
                    userSessionRepository = userSessionRepository,
                    financeRepository = financeRepository,
                    roomRepository = roomRepository,
                    userRepository = userRepository
                )
            }
        }
        private const val TAG = "FinanceManagerViewModel"
    }
}