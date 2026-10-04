package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PaymentMethodEntity
import com.example.data.model.AccountStatus
import com.example.data.model.TradeDirection
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavDestination(val label: String, val isAdminRoute: Boolean = false) {
    AUTH("Sign In / Register"),

    // User Routes
    DASHBOARD("Dashboard"),
    TRADE("Trade"),
    POSITIONS("Positions"),
    DEPOSIT("Deposit"),
    WITHDRAW("Withdraw"),
    TRANSACTIONS("Transactions"),
    NOTIFICATIONS("Alerts"),
    PROFILE("Profile"),

    // Admin Routes
    ADMIN_DASHBOARD("Admin Overview", true),
    ADMIN_DEPOSITS("Deposits Review", true),
    ADMIN_WITHDRAWALS("Withdrawals Review", true),
    ADMIN_USERS("User Management", true),
    ADMIN_TRADES("All Trades", true),
    ADMIN_PAYMENT_METHODS("Payment Methods", true),
    ADMIN_AUDIT_LOGS("Audit & Backups", true)
}

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = TradingRepository.getInstance(db)

    // Current navigation destination (defaults to AUTH so Login/Signup screen opens first)
    private val _currentScreen = MutableStateFlow(AppNavDestination.AUTH)
    val currentScreen: StateFlow<AppNavDestination> = _currentScreen.asStateFlow()

    // Navigation backstack for back handling
    private val backStack = mutableListOf<AppNavDestination>()

    // Selected trading pair
    private val _selectedAssetSymbol = MutableStateFlow("BTC/USDT")
    val selectedAssetSymbol: StateFlow<String> = _selectedAssetSymbol.asStateFlow()

    // UI Message Banner
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Quick switch / auth modal state
    private val _showSwitchAccountDialog = MutableStateFlow(false)
    val showSwitchAccountDialog: StateFlow<Boolean> = _showSwitchAccountDialog.asStateFlow()

    // Selected deposit for full admin review / screenshot modal
    private val _reviewingDepositId = MutableStateFlow<String?>(null)
    val reviewingDepositId: StateFlow<String?> = _reviewingDepositId.asStateFlow()

    // User Data Flows
    val currentUser = repository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val currentWallet = repository.currentWallet.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val marketAssets = repository.marketAssets
    val myOpenTrades = repository.myOpenTrades.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val myTrades = repository.myTrades.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val myDeposits = repository.myDeposits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val myWithdrawals = repository.myWithdrawals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val myNotifications = repository.myNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Data Flows
    val allUsers = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTrades = repository.allTrades.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDeposits = repository.allDeposits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pendingDeposits = repository.pendingDeposits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allWithdrawals = repository.allWithdrawals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pendingWithdrawals = repository.pendingWithdrawals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPaymentMethods = repository.allPaymentMethods.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAuditLogs = repository.allAuditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // On-screen Real-Time Trade Result Notification Alert with exact time
    val latestSettlementAlert = repository.latestSettlementAlert

    fun dismissSettlementAlert() {
        repository.dismissSettlementAlert()
    }

    // Admin Security Lock Dialog state (Locked with Furqan@786)
    private val _showAdminPasswordDialog = MutableStateFlow(false)
    val showAdminPasswordDialog: StateFlow<Boolean> = _showAdminPasswordDialog.asStateFlow()

    fun openAdminPasswordPrompt() {
        _showAdminPasswordDialog.value = true
    }

    fun closeAdminPasswordPrompt() {
        _showAdminPasswordDialog.value = false
    }

    fun unlockAdminWithPassword(password: String): Boolean {
        val cleanPass = password.trim()
        if (cleanPass == "Furqan@786") {
            viewModelScope.launch {
                repository.adminLogin("Fadi Raees", "Furqan@786")
                _showAdminPasswordDialog.value = false
                _currentScreen.value = AppNavDestination.ADMIN_DASHBOARD
                emitMessage("Admin Portal Unlocked! Welcome Fadi Raees (MRG Company Master Admin)")
            }
            return true
        } else {
            return false
        }
    }

    fun navigateTo(dest: AppNavDestination) {
        if (_currentScreen.value != dest) {
            backStack.add(_currentScreen.value)
            _currentScreen.value = dest
        }
    }

    fun handleBack(): Boolean {
        return if (backStack.isNotEmpty()) {
            _currentScreen.value = backStack.removeAt(backStack.lastIndex)
            true
        } else {
            false
        }
    }

    fun selectAsset(symbol: String) {
        _selectedAssetSymbol.value = symbol
    }

    fun setSwitchAccountDialog(visible: Boolean) {
        _showSwitchAccountDialog.value = visible
    }

    fun setReviewingDeposit(depositId: String?) {
        _reviewingDepositId.value = depositId
    }

    fun switchAccount(userId: String) {
        repository.switchUser(userId)
        _showSwitchAccountDialog.value = false
        emitMessage("Switched user session")
    }

    fun registerNewAccount(username: String, email: String, phone: String) {
        viewModelScope.launch {
            val result = repository.registerUser(username, email, phone)
            result.onSuccess {
                _showSwitchAccountDialog.value = false
                _currentScreen.value = AppNavDestination.DASHBOARD
                emitMessage("Account created with 0.00 USDT balance. Please deposit funds via TRON (TRC20) to start trading.")
            }.onFailure { err ->
                emitMessage("Registration error: ${err.message}")
            }
        }
    }

    fun adminLogin(userId: String, pass: String) {
        viewModelScope.launch {
            val res = repository.adminLogin(userId, pass)
            res.onSuccess {
                _showSwitchAccountDialog.value = false
                _currentScreen.value = AppNavDestination.ADMIN_DASHBOARD
                emitMessage("Welcome Fadi Raees! MRG Company Master Admin Portal active.")
            }.onFailure { err ->
                emitMessage("Login failed: ${err.message}")
            }
        }
    }

    fun loginUser(identifier: String, pass: String) {
        viewModelScope.launch {
            val res = repository.userLogin(identifier, pass)
            res.onSuccess {
                _showSwitchAccountDialog.value = false
                _currentScreen.value = AppNavDestination.DASHBOARD
                emitMessage("Logged in successfully. Welcome back!")
            }.onFailure { err ->
                emitMessage("Login failed: ${err.message}")
            }
        }
    }

    fun quickTraderLogin() {
        viewModelScope.launch {
            val res = repository.quickTraderDemo()
            res.onSuccess {
                _showSwitchAccountDialog.value = false
                _currentScreen.value = AppNavDestination.DASHBOARD
                emitMessage("Logged in as Demo Trader (USR-1082)")
            }.onFailure { err ->
                emitMessage("Quick login failed: ${err.message}")
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentScreen.value = AppNavDestination.AUTH
        emitMessage("Logged out successfully.")
    }

    fun executeTrade(
        assetSymbol: String,
        direction: TradeDirection,
        amount: Double,
        leverage: Int,
        stopLoss: Double?,
        takeProfit: Double?,
        durationSeconds: Int = 60,
        profitPercentage: Double = 25.0
    ) {
        viewModelScope.launch {
            val result = repository.openTrade(assetSymbol, direction, amount, leverage, stopLoss, takeProfit, durationSeconds, profitPercentage)
            result.onSuccess {
                emitMessage("Position opened: $assetSymbol (${direction.label}) • +${profitPercentage.toInt()}% Profit Target")
            }.onFailure { err ->
                emitMessage("Failed to execute trade: ${err.message}")
            }
        }
    }

    fun closeTrade(tradeId: String) {
        viewModelScope.launch {
            val res = repository.closePosition(tradeId)
            res.onSuccess {
                emitMessage("Position successfully closed and settled")
            }.onFailure { err ->
                emitMessage("Error closing position: ${err.message}")
            }
        }
    }

    fun submitDeposit(amount: Double, network: String, walletAddress: String, txHash: String, note: String?, screenshotData: String?) {
        viewModelScope.launch {
            val res = repository.submitDeposit(amount, network, walletAddress, txHash, note, screenshotData)
            res.onSuccess { id ->
                emitMessage("Deposit request $id submitted! Review time: 2-5 mins.")
            }.onFailure { err ->
                emitMessage("Deposit error: ${err.message}")
            }
        }
    }

    fun submitWithdrawal(amount: Double, network: String, destinationAddress: String) {
        viewModelScope.launch {
            val res = repository.submitWithdrawal(amount, network, destinationAddress)
            res.onSuccess { id ->
                emitMessage("Withdrawal request $id submitted for processing.")
            }.onFailure { err ->
                emitMessage("Withdrawal error: ${err.message}")
            }
        }
    }

    fun updateUserProfile(username: String, email: String, phone: String) {
        viewModelScope.launch {
            val uid = repository.currentUserId.value ?: return@launch
            if (username.isBlank()) {
                emitMessage("Full name/Username cannot be empty")
                return@launch
            }
            if (email.isBlank()) {
                emitMessage("Email (Gmail) cannot be empty")
                return@launch
            }
            val res = repository.updateUserProfile(uid, username.trim(), email.trim(), phone.trim())
            res.onSuccess {
                emitMessage("Profile information updated successfully")
            }.onFailure { err ->
                emitMessage("Failed to update profile: ${err.message}")
            }
        }
    }

    fun toggleTwoFactor(currentlyEnabled: Boolean) {
        viewModelScope.launch {
            val uid = repository.currentUserId.value ?: return@launch
            val res = repository.toggleTwoFactor(uid, !currentlyEnabled)
            res.onSuccess {
                emitMessage(if (!currentlyEnabled) "Two-Factor Authentication enabled" else "Two-Factor Authentication disabled")
            }.onFailure { err ->
                emitMessage("Failed to toggle 2FA: ${err.message}")
            }
        }
    }

    fun approveDeposit(depositId: String, note: String = "Blockchain TX confirmed") {
        viewModelScope.launch {
            val adminId = currentUser.value?.id ?: "ADM-001"
            val res = repository.approveDeposit(depositId, adminId, note)
            res.onSuccess {
                _reviewingDepositId.value = null
                emitMessage("Deposit $depositId approved. Funds credited to user wallet.")
            }.onFailure { err ->
                emitMessage("Approval failed: ${err.message}")
            }
        }
    }

    fun rejectDeposit(depositId: String, reason: String) {
        viewModelScope.launch {
            val adminId = currentUser.value?.id ?: "ADM-001"
            val res = repository.rejectDeposit(depositId, adminId, reason)
            res.onSuccess {
                _reviewingDepositId.value = null
                emitMessage("Deposit $depositId rejected. Reason recorded in audit log.")
            }.onFailure { err ->
                emitMessage("Rejection failed: ${err.message}")
            }
        }
    }

    fun completeWithdrawal(withdrawalId: String, txHash: String, notes: String) {
        viewModelScope.launch {
            val adminId = currentUser.value?.id ?: "ADM-001"
            val res = repository.completeWithdrawal(withdrawalId, adminId, txHash, notes)
            res.onSuccess {
                emitMessage("Withdrawal $withdrawalId completed and recorded.")
            }.onFailure { err ->
                emitMessage("Processing failed: ${err.message}")
            }
        }
    }

    fun rejectWithdrawal(withdrawalId: String, reason: String) {
        viewModelScope.launch {
            val adminId = currentUser.value?.id ?: "ADM-001"
            val res = repository.rejectWithdrawal(withdrawalId, adminId, reason)
            res.onSuccess {
                emitMessage("Withdrawal rejected and funds refunded to user available balance.")
            }.onFailure { err ->
                emitMessage("Rejection error: ${err.message}")
            }
        }
    }

    fun updateUserStatus(userId: String, newStatus: AccountStatus, reason: String) {
        viewModelScope.launch {
            val adminId = currentUser.value?.id ?: "ADM-001"
            val res = repository.updateUserStatus(userId, newStatus, adminId, reason)
            res.onSuccess {
                emitMessage("User status updated to ${newStatus.displayName}")
            }.onFailure { err ->
                emitMessage("Status update failed: ${err.message}")
            }
        }
    }

    fun adjustUserBalance(userId: String, delta: Double, reason: String) {
        viewModelScope.launch {
            val adminId = currentUser.value?.id ?: "ADM-001"
            val res = repository.adjustUserBalance(userId, delta, adminId, reason)
            res.onSuccess {
                emitMessage("Balance adjusted and logged to immutable audit ledger.")
            }.onFailure { err ->
                emitMessage("Adjustment failed: ${err.message}")
            }
        }
    }

    fun adminDepositToUser(userId: String, amount: Double, note: String) {
        viewModelScope.launch {
            val adminId = currentUser.value?.id ?: "Fadi Raees"
            val res = repository.adminDepositToUser(userId, amount, adminId, note)
            res.onSuccess {
                emitMessage("Success: Deposited $amount USDT into user $userId")
            }.onFailure { err ->
                emitMessage("Deposit error: ${err.message}")
            }
        }
    }

    fun adminSetTradeWin(tradeId: String, profitPct: Double = 85.0) {
        viewModelScope.launch {
            val adminId = currentUser.value?.id ?: "Fadi Raees"
            val res = repository.adminSetTradeWin(tradeId, adminId, profitPct)
            res.onSuccess {
                emitMessage("Trade $tradeId settled as WIN (+${profitPct}%)")
            }.onFailure { err ->
                emitMessage("Error: ${err.message}")
            }
        }
    }

    fun adminSetTradeLoss(tradeId: String, reason: String = "Admin Market Decision") {
        viewModelScope.launch {
            val adminId = currentUser.value?.id ?: "Fadi Raees"
            val res = repository.adminSetTradeLoss(tradeId, adminId, reason)
            res.onSuccess {
                emitMessage("Trade $tradeId settled as LOSS")
            }.onFailure { err ->
                emitMessage("Error: ${err.message}")
            }
        }
    }

    fun loginWithGoogle(email: String, displayName: String) {
        viewModelScope.launch {
            val res = repository.loginOrRegisterWithGoogle(email, displayName)
            res.onSuccess { uid ->
                _showSwitchAccountDialog.value = false
                _currentScreen.value = AppNavDestination.DASHBOARD
                emitMessage("Logged in as $displayName (0.00 USDT balance). Deposit to trade!")
            }.onFailure { err ->
                emitMessage("Login failed: ${err.message}")
            }
        }
    }

    fun savePaymentMethod(pm: PaymentMethodEntity) {
        viewModelScope.launch {
            val adminId = currentUser.value?.id ?: "ADM-001"
            val res = repository.savePaymentMethod(pm, adminId)
            res.onSuccess {
                emitMessage("Payment method saved successfully")
            }.onFailure { err ->
                emitMessage("Failed to save payment method: ${err.message}")
            }
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markNotificationsRead()
        }
    }

    fun exportDatabaseBackup(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDatabaseBackupJson()
            onExportReady(json)
            emitMessage("Database snapshot exported successfully")
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }
}
