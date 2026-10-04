package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DepositEntity
import com.example.data.local.UserEntity
import com.example.data.local.WalletEntity
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val isLoading: Boolean = false,
    val uniqueId: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val accountStatus: String = "ACTIVE",
    val role: String = "TRADER",
    val isVerified: Boolean = true,
    val twoFactorEnabled: Boolean = false,
    val registeredDate: Long = 0L,
    val totalBalance: Double = 0.0,
    val availableBalance: Double = 0.0,
    val lockedMargin: Double = 0.0,
    val totalDeposited: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val totalProfitLoss: Double = 0.0,
    val isZeroBalance: Boolean = true,
    val hasDeposited: Boolean = false,
    val depositPromptMessage: String = "Account balance is 0.00 USDT. Please make a deposit via TRON (TRC20) to fund your account and start trading.",
    val recentDeposits: List<DepositEntity> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = TradingRepository.getInstance(db)

    // Current active user ID
    val currentUserId: StateFlow<String?> = repository.currentUserId

    // Direct database flow observation
    val userFlow: StateFlow<UserEntity?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val walletFlow: StateFlow<WalletEntity?> = repository.currentWallet
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val depositsFlow: StateFlow<List<DepositEntity>> = repository.myDeposits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined UI State
    val uiState: StateFlow<UserProfileUiState> = combine(
        userFlow,
        walletFlow,
        depositsFlow
    ) { user, wallet, deposits ->
        val balance = wallet?.totalBalance ?: 0.0
        val deposited = wallet?.totalDeposited ?: 0.0
        val isZero = balance <= 0.0

        UserProfileUiState(
            isLoading = user == null,
            uniqueId = user?.id ?: "",
            username = user?.username ?: "",
            email = user?.email ?: "",
            phone = user?.phone ?: "",
            accountStatus = user?.accountStatus ?: "ACTIVE",
            role = user?.role ?: "TRADER",
            isVerified = user?.isVerified ?: true,
            twoFactorEnabled = user?.twoFactorEnabled ?: false,
            registeredDate = user?.createdAt ?: 0L,
            totalBalance = balance,
            availableBalance = wallet?.availableBalance ?: 0.0,
            lockedMargin = wallet?.lockedMargin ?: 0.0,
            totalDeposited = deposited,
            totalWithdrawn = wallet?.totalWithdrawn ?: 0.0,
            totalProfitLoss = wallet?.totalProfitLoss ?: 0.0,
            isZeroBalance = isZero,
            hasDeposited = deposited > 0.0,
            depositPromptMessage = if (isZero) {
                "Account balance is 0.00 USDT. Please make a deposit via TRON (TRC20) to fund your account and start trading."
            } else {
                "Account funded: ${String.format(java.util.Locale.US, "%,.2f", balance)} USDT available."
            },
            recentDeposits = deposits.take(5)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileUiState(isLoading = true))

    private val _eventMessage = MutableSharedFlow<String>()
    val eventMessage: SharedFlow<String> = _eventMessage.asSharedFlow()

    fun updateProfile(newUsername: String, newEmail: String, newPhone: String) {
        viewModelScope.launch {
            val uid = currentUserId.value ?: return@launch
            if (newUsername.isBlank()) {
                _eventMessage.emit("Username cannot be empty")
                return@launch
            }
            if (newEmail.isBlank()) {
                _eventMessage.emit("Email cannot be empty")
                return@launch
            }
            val res = repository.updateUserProfile(uid, newUsername.trim(), newEmail.trim(), newPhone.trim())
            res.onSuccess {
                _eventMessage.emit("Profile details updated successfully")
            }.onFailure {
                _eventMessage.emit("Failed to update profile: ${it.message}")
            }
        }
    }

    fun toggleTwoFactor(currentlyEnabled: Boolean) {
        viewModelScope.launch {
            val uid = currentUserId.value ?: return@launch
            val res = repository.toggleTwoFactor(uid, !currentlyEnabled)
            res.onSuccess {
                _eventMessage.emit(if (!currentlyEnabled) "Two-Factor Authentication enabled" else "Two-Factor Authentication disabled")
            }.onFailure {
                _eventMessage.emit("Failed to toggle 2FA: ${it.message}")
            }
        }
    }

    fun switchUser(newUserId: String) {
        repository.switchUser(newUserId)
        viewModelScope.launch {
            _eventMessage.emit("Switched to user $newUserId")
        }
    }
}
