// Create this file: com/example/sharespace/ui/screens/finance/FinanceManagerViewModel.kt
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
import com.example.sharespace.core.data.repository.UserSessionRepository
import com.example.sharespace.core.data.repository.dto.finance.ApiTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FinanceManagerViewModel(
    private val userSessionRepository: UserSessionRepository,
    private val financeRepository: FinanceRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    // State for transactions
    private val _transactions = MutableStateFlow<List<ApiTransaction>>(emptyList())
    val transactions: StateFlow<List<ApiTransaction>> = _transactions

    // State for roommates
    private val _roommates = MutableStateFlow<List<com.example.sharespace.core.data.repository.dto.users.ApiUser>>(emptyList())
    val roommates: StateFlow<List<com.example.sharespace.core.data.repository.dto.users.ApiUser>> = _roommates


    // Loading state
    private val _isLoading = mutableStateOf(false)
    val isLoading = _isLoading

    // Error state
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage = _errorMessage

    // Update your FinanceManagerViewModel loadTransactions() method with debugging:

    fun loadTransactions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val token = userSessionRepository.userTokenFlow.first()
                val activeRoomId = userSessionRepository.activeRoomIdFlow.first()

                Log.d(TAG, "=== FINANCE MANAGER DEBUG ===")
                Log.d(TAG, "Token exists: ${token != null}")
                Log.d(TAG, "Active Room ID: $activeRoomId")

                if (token == null) {
                    _errorMessage.value = "No authentication token found"
                    return@launch
                }

                if (activeRoomId == null) {
                    _errorMessage.value = "No active room selected"
                    return@launch
                }

                // Load both transactions and roommates
                val transactionList = financeRepository.getTransactionList(token, activeRoomId)
                val roommatesList = roomRepository.getRoomMembers(token, activeRoomId)

                _transactions.value = transactionList
                _roommates.value = roommatesList

                Log.d(TAG, "✅ Successfully loaded ${transactionList.size} transactions")
                Log.d(TAG, "✅ Successfully loaded ${roommatesList.size} roommates")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading data: ${e.message}", e)
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun createBill(
        title: String,
        category: String,
        amount: String,
        payerId: String,
        users: List<Map<String, String>>, // List of maps with "user_id" and "amount_due"
        frequency: String,
        repeat: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val token = userSessionRepository.userTokenFlow.first()
                val activeRoomId = userSessionRepository.activeRoomIdFlow.first()

                if (token == null || activeRoomId == null) {
                    _errorMessage.value = "Missing authentication or room information"
                    return@launch
                }

                // Convert users list to ApiBillUser objects
                val billUsers = users.map { userMap ->
                    com.example.sharespace.core.data.repository.dto.finance.ApiBillUser(
                        userId = userMap["user_id"] ?: "",
                        amountDue = userMap["amount_due"] ?: ""
                    )
                }

                val request = com.example.sharespace.core.data.repository.dto.finance.ApiCreateBillRequest(
                    title = title,
                    category = category,
                    amount = amount,
                    payerId = payerId,
                    users = billUsers,
                    frequency = frequency,
                    repeat = repeat
                )

                val response = financeRepository.createBill(token, activeRoomId, request)
                Log.d(TAG, "Bill created successfully: ${response.message}")

                // Reload transactions to show the new bill
                loadTransactions()

            } catch (e: Exception) {
                Log.e(TAG, "Error creating bill", e)
                _errorMessage.value = "Failed to create bill: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)
                val userSessionRepository = application.container.userSessionRepository
                val financeRepository = application.container.financeRepository
                val roomRepository = application.container.roomRepository // Add this
                FinanceManagerViewModel(
                    userSessionRepository = userSessionRepository,
                    financeRepository = financeRepository,
                    roomRepository = roomRepository // Add this
                )
            }
        }
        private const val TAG = "FinanceManagerViewModel"
    }
}