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
import com.example.sharespace.core.data.repository.dto.finance.ApiCreateBillRequest
import com.example.sharespace.core.data.repository.dto.finance.ApiBillUser
import com.example.sharespace.core.data.repository.dto.users.ApiUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

data class RoommateSplit(
    val user: ApiUser,
    val amount: String = "0"
)

class AddBillViewModel(
    private val userSessionRepository: UserSessionRepository,
    private val financeRepository: FinanceRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    // State for roommates
    private val _roommates = MutableStateFlow<List<RoommateSplit>>(emptyList())
    val roommates: StateFlow<List<RoommateSplit>> = _roommates

    // Loading state
    private val _isLoading = mutableStateOf(false)
    val isLoading = _isLoading

    // Error state
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage = _errorMessage

    // Success state
    private val _billCreated = mutableStateOf(false)
    val billCreated = _billCreated

    fun loadRoommates() {
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

                val roommatesList = roomRepository.getRoomMembers(token, activeRoomId)
                _roommates.value = roommatesList.map { user ->
                    RoommateSplit(user = user, amount = "0")
                }

                Log.d(TAG, "✅ Loaded ${roommatesList.size} roommates for bill creation")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading roommates: ${e.message}", e)
                _errorMessage.value = "Failed to load roommates: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRoommateAmount(userId: Int, amount: String) {
        _roommates.value = _roommates.value.map { split ->
            if (split.user.id == userId) {
                split.copy(amount = amount)
            } else {
                split
            }
        }
    }

    fun splitEvenly(totalAmount: String) {
        val total = totalAmount.toDoubleOrNull() ?: 0.0
        val roommates = _roommates.value
        if (roommates.isNotEmpty() && total > 0) {
            val splitAmount = String.format("%.2f", total / roommates.size)
            _roommates.value = roommates.map { split ->
                split.copy(amount = splitAmount)
            }
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

                // Validate inputs
                if (title.isBlank()) {
                    _errorMessage.value = "Title is required"
                    return@launch
                }

                val amount = totalAmount.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    _errorMessage.value = "Please enter a valid amount"
                    return@launch
                }

                // Get users with non-zero amounts
                val usersWithAmounts = _roommates.value.mapNotNull { split ->
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

                // Validate total matches sum of user amounts (with small tolerance for floating point precision)
                val sumOfAmounts = usersWithAmounts.sumOf { it.amountDue.toDoubleOrNull() ?: 0.0 }
                if (abs(sumOfAmounts - amount) > 0.01) {
                    _errorMessage.value = "Total amount (${String.format("%.2f", amount)}) must equal sum of individual amounts (${String.format("%.2f", sumOfAmounts)})"
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

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetBillCreated() {
        _billCreated.value = false
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ShareSpaceApplication)
                val userSessionRepository = application.container.userSessionRepository
                val financeRepository = application.container.financeRepository
                val roomRepository = application.container.roomRepository
                AddBillViewModel(
                    userSessionRepository = userSessionRepository,
                    financeRepository = financeRepository,
                    roomRepository = roomRepository
                )
            }
        }
        private const val TAG = "AddBillViewModel"
    }
}