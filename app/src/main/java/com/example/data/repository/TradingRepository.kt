package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

class TradingRepository(private val db: AppDatabase) {

    companion object {
        @Volatile
        private var INSTANCE: TradingRepository? = null

        fun getInstance(db: AppDatabase): TradingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TradingRepository(db).also { INSTANCE = it }
            }
        }

        private fun generateInitialSparkline(base: Double): List<Double> {
            val list = mutableListOf<Double>()
            var current = base * 0.98
            for (i in 0 until 20) {
                current += (Random.nextDouble() - 0.48) * (base * 0.005)
                list.add(current)
            }
            list[list.lastIndex] = base
            return list
        }
    }

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Currently logged-in user ID (null by default so app opens to Auth Screen)
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    val currentUser: Flow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else db.userDao().getUserById(id)
    }

    val currentWallet: Flow<WalletEntity?> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else db.walletDao().getWalletForUser(id)
    }

    val myTrades: Flow<List<TradeEntity>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else db.tradeDao().getTradesForUser(id)
    }

    val myOpenTrades: Flow<List<TradeEntity>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else db.tradeDao().getOpenTradesForUser(id)
    }

    val myDeposits: Flow<List<DepositEntity>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else db.depositDao().getDepositsForUser(id)
    }

    val myWithdrawals: Flow<List<WithdrawalEntity>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else db.withdrawalDao().getWithdrawalsForUser(id)
    }

    val myNotifications: Flow<List<NotificationEntity>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else db.notificationDao().getNotificationsForUser(id)
    }

    // Admin Level Flows
    val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsers()
    val allTrades: Flow<List<TradeEntity>> = db.tradeDao().getAllTrades()
    val allDeposits: Flow<List<DepositEntity>> = db.depositDao().getAllDeposits()
    val pendingDeposits: Flow<List<DepositEntity>> = db.depositDao().getPendingDeposits()
    val allWithdrawals: Flow<List<WithdrawalEntity>> = db.withdrawalDao().getAllWithdrawals()
    val pendingWithdrawals: Flow<List<WithdrawalEntity>> = db.withdrawalDao().getPendingWithdrawals()
    val allPaymentMethods: Flow<List<PaymentMethodEntity>> = db.paymentMethodDao().getAllPaymentMethods()
    val activePaymentMethods: Flow<List<PaymentMethodEntity>> = db.paymentMethodDao().getActivePaymentMethods()
    val allAuditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getAllAuditLogs()

    // Real-Time Trade Settlement On-Screen Notification with exact time
    private val _latestSettlementAlert = MutableStateFlow<TradeSettlementAlert?>(null)
    val latestSettlementAlert: StateFlow<TradeSettlementAlert?> = _latestSettlementAlert.asStateFlow()

    fun dismissSettlementAlert() {
        _latestSettlementAlert.value = null
    }

    // Real-Time Market Ticker State
    private val _marketAssets = MutableStateFlow(
        listOf(
            MarketAsset("BTC/USDT", "Bitcoin", 67450.00, +2.45, 68100.00, 65800.00, 38402910.0, generateInitialSparkline(67450.0)),
            MarketAsset("ETH/USDT", "Ethereum", 3520.40, +1.82, 3580.00, 3440.00, 19284100.0, generateInitialSparkline(3520.0)),
            MarketAsset("SOL/USDT", "Solana", 154.20, +5.12, 158.00, 146.50, 8934120.0, generateInitialSparkline(154.0)),
            MarketAsset("XRP/USDT", "Ripple", 0.5840, -0.65, 0.5980, 0.5750, 4829100.0, generateInitialSparkline(0.58)),
            MarketAsset("BNB/USDT", "BNB", 592.10, +0.94, 598.00, 584.00, 6210400.0, generateInitialSparkline(592.0)),
            MarketAsset("EUR/USD", "Euro / USD", 1.0850, +0.15, 1.0890, 1.0820, 14205000.0, generateInitialSparkline(1.085), isCrypto = false),
            MarketAsset("XAU/USD", "Gold / USD", 2645.80, +0.78, 2658.00, 2632.00, 12504900.0, generateInitialSparkline(2645.0), isCrypto = false)
        )
    )
    val marketAssets: StateFlow<List<MarketAsset>> = _marketAssets.asStateFlow()

    init {
        // Start simulated live market price fluctuations & PnL updates
        startMarketSimulation()
        repositoryScope.launch {
            ensureDefaultAccounts()
        }
    }

    private suspend fun ensureDefaultAccounts() {
        try {
            if (db.userDao().getUserByIdSync("Fadi Raees") == null) {
                db.userDao().insertUser(
                    UserEntity(
                        id = "Fadi Raees",
                        username = "Fadi Raees",
                        email = "admin@mrgcompany.io",
                        phone = "+1 (800) 555-0100",
                        role = "SUPER_ADMIN",
                        accountStatus = "ACTIVE",
                        createdAt = System.currentTimeMillis() - 90L * 86400000L,
                        lastLoginAt = System.currentTimeMillis(),
                        isVerified = true,
                        twoFactorEnabled = true,
                        internalNotes = "MRG Company Master Administrator. Password: Furqan@786"
                    )
                )
                db.walletDao().insertWallet(
                    WalletEntity(
                        userId = "Fadi Raees",
                        totalBalance = 100000.0,
                        availableBalance = 100000.0,
                        lockedMargin = 0.0,
                        totalDeposited = 100000.0,
                        totalWithdrawn = 0.0,
                        totalProfitLoss = 0.0
                    )
                )
            }
        } catch (_: Exception) {}
    }

    private fun startMarketSimulation() {
        repositoryScope.launch {
            while (isActive) {
                delay(2500)
                updateMarketPrices()
            }
        }
    }

    private suspend fun updateMarketPrices() {
        val currentList = _marketAssets.value.toMutableList()
        val updated = currentList.map { asset ->
            val deltaPct = (Random.nextDouble() - 0.49) * 0.004 // subtle realistic market variance
            val newPrice = (asset.price * (1.0 + deltaPct)).let {
                if (asset.price < 1.0) String.format(Locale.US, "%.4f", it).toDouble()
                else String.format(Locale.US, "%.2f", it).toDouble()
            }
            val newSparkline = (asset.sparkline.drop(1) + newPrice).takeLast(20)
            asset.copy(
                price = newPrice,
                change24h = ((newPrice - (asset.sparkline.firstOrNull() ?: newPrice)) / (asset.sparkline.firstOrNull() ?: newPrice) * 100.0)
                    .let { String.format(Locale.US, "%.2f", it).toDouble() },
                sparkline = newSparkline
            )
        }
        _marketAssets.value = updated

        // Recalculate open positions PnL & enforce Auto-Loss policy if admin doesn't mark WIN
        try {
            val openTrades = db.tradeDao().getOpenTradesSync()
            val priceMap = updated.associate { it.symbol to it.price }
            val now = System.currentTimeMillis()

            openTrades.forEach { trade ->
                val currentPrice = priceMap[trade.assetSymbol] ?: trade.currentPrice
                val priceDiff = currentPrice - trade.entryPrice
                val isUp = trade.direction == "UP" || trade.direction == "BUY_LONG"
                val multiplier = if (isUp) 1.0 else -1.0
                val rawPnl = (priceDiff / trade.entryPrice) * trade.positionSize * multiplier
                val pnlPercentage = (rawPnl / trade.amount) * 100.0

                // User Rule: "ager koi admin trde win na kar to wo trade loss ho ja ni ha"
                // Trade execution window set by user (e.g. 30s, 60s, 120s, 300s). If admin has not marked trade as WIN within duration, it automatically settles as LOSS.
                val tradeAge = now - trade.createdAt
                val durationMillis = trade.durationSeconds * 1000L

                if (tradeAge >= durationMillis) {
                    adminSetTradeLoss(trade.id, "SYSTEM", "Trade duration expired (${trade.durationSeconds}s) - Defaulted to LOSS")
                } else {
                    // Check automated Stop Loss or Take Profit
                    val shouldCloseForTp = trade.takeProfit != null && (
                        (isUp && currentPrice >= trade.takeProfit) ||
                        (!isUp && currentPrice <= trade.takeProfit)
                    )
                    val shouldCloseForSl = trade.stopLoss != null && (
                        (isUp && currentPrice <= trade.stopLoss) ||
                        (!isUp && currentPrice >= trade.stopLoss)
                    )

                    if (shouldCloseForTp || shouldCloseForSl) {
                        val reason = if (shouldCloseForTp) "Take Profit Triggered at $currentPrice" else "Stop Loss Triggered at $currentPrice"
                        closePositionInternal(trade.id, currentPrice, reason, "SYSTEM")
                    } else {
                        db.tradeDao().updateTrade(
                            trade.copy(
                                currentPrice = currentPrice,
                                pnl = String.format(Locale.US, "%.2f", rawPnl).toDouble(),
                                pnlPercentage = String.format(Locale.US, "%.2f", pnlPercentage).toDouble()
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun switchUser(userId: String) {
        _currentUserId.value = userId
    }

    suspend fun adminLogin(userId: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanId = userId.trim()
        val cleanPass = pass.trim()
        if ((cleanId.equals("Fadi Raees", ignoreCase = true) || cleanId.equals("admin", ignoreCase = true) || cleanId.equals("ADM-001", ignoreCase = true)) &&
            cleanPass == "Furqan@786"
        ) {
            ensureDefaultAccounts()
            _currentUserId.value = "Fadi Raees"
            Result.success("Fadi Raees")
        } else {
            Result.failure(Exception("Invalid Admin Credentials. User ID must be 'Fadi Raees' and Password 'Furqan@786'"))
        }
    }

    suspend fun userLogin(identifier: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        val clean = identifier.trim()
        val user = db.userDao().getUserByEmail(clean)
            ?: db.userDao().getUserByIdSync(clean)
            ?: db.userDao().getAllUsersSync().find { it.username.equals(clean, ignoreCase = true) }

        if (user != null) {
            _currentUserId.value = user.id
            Result.success(user.id)
        } else {
            Result.failure(Exception("Account not found with username/email '$identifier'. Please sign up or use Quick Trader Demo."))
        }
    }

    suspend fun quickTraderDemo(): Result<String> = withContext(Dispatchers.IO) {
        val trader = db.userDao().getUserByIdSync("USR-1082")
            ?: db.userDao().getAllUsersSync().firstOrNull { it.role == "TRADER" }
        if (trader != null) {
            _currentUserId.value = trader.id
            Result.success(trader.id)
        } else {
            Result.failure(Exception("Trader demo account not found"))
        }
    }

    fun logout() {
        _currentUserId.value = null
    }

    suspend fun registerUser(username: String, email: String, phone: String): Result<String> = withContext(Dispatchers.IO) {
        val existing = db.userDao().getUserByEmail(email)
        if (existing != null) {
            return@withContext Result.failure(Exception("An account with this email already exists"))
        }
        val newId = "USR-${Random.nextInt(10000, 99999)}"
        val user = UserEntity(
            id = newId,
            username = username,
            email = email,
            phone = phone,
            role = "TRADER",
            accountStatus = "ACTIVE",
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis(),
            isVerified = true
        )
        db.userDao().insertUser(user)
        val initialWallet = WalletEntity(
            userId = newId,
            totalBalance = 0.00, // Balance remains 0.00 until user makes a deposit
            availableBalance = 0.00,
            lockedMargin = 0.00,
            totalDeposited = 0.00,
            totalWithdrawn = 0.00,
            totalProfitLoss = 0.00
        )
        db.walletDao().insertWallet(initialWallet)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = newId,
                title = "Welcome to ApexTrade",
                message = "Your trading account has been registered with 0.00 USDT balance. Please deposit funds via TRON (TRC20) to fund your account and start trading.",
                type = "SYSTEM"
            )
        )

        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = newId,
                actorRole = "TRADER",
                action = "USER_REGISTER",
                entityType = "USER",
                entityId = newId,
                previousValue = "NONE",
                newValue = "username: $username, email: $email",
                reason = "New self-serve trader account creation",
                clientInfo = "Apex Android App"
            )
        )

        _currentUserId.value = newId
        Result.success(newId)
    }

    suspend fun openTrade(
        assetSymbol: String,
        direction: TradeDirection,
        amount: Double,
        leverage: Int,
        stopLoss: Double?,
        takeProfit: Double?,
        durationSeconds: Int = 60,
        profitPercentage: Double = 85.0
    ): Result<String> = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value ?: return@withContext Result.failure(Exception("Please log in to trade"))
        val wallet = db.walletDao().getWalletForUserSync(userId) ?: return@withContext Result.failure(Exception("Wallet not found"))

        if (wallet.availableBalance <= 0.0) {
            return@withContext Result.failure(Exception("Your balance is 0.00 USDT. You cannot trade until you deposit funds. Please make a deposit via TRON (TRC20) to activate trading."))
        }

        if (amount < 100.0) {
            return@withContext Result.failure(Exception("Minimum trade start amount is 100 USDT."))
        }

        if (wallet.availableBalance < amount) {
            return@withContext Result.failure(Exception("Insufficient available balance. Required: $amount USDT, Available: ${wallet.availableBalance} USDT"))
        }

        val user = db.userDao().getUserByIdSync(userId)
        val username = user?.username ?: "Trader"
        val asset = _marketAssets.value.find { it.symbol == assetSymbol } ?: return@withContext Result.failure(Exception("Asset not found"))
        val entryPrice = asset.price
        val positionSize = amount * leverage
        val tradeId = "TRD-${Random.nextInt(10000, 99999)}"

        // 1. Deduct margin from available balance
        val updatedWallet = wallet.copy(
            availableBalance = wallet.availableBalance - amount,
            lockedMargin = wallet.lockedMargin + amount
        )
        db.walletDao().updateWallet(updatedWallet)

        // 2. Insert Trade
        val trade = TradeEntity(
            id = tradeId,
            userId = userId,
            username = username,
            assetSymbol = assetSymbol,
            direction = direction.name,
            amount = amount,
            leverage = leverage,
            positionSize = positionSize,
            entryPrice = entryPrice,
            currentPrice = entryPrice,
            status = "OPEN",
            durationSeconds = durationSeconds,
            profitPercentage = profitPercentage,
            pnl = 0.0,
            pnlPercentage = 0.0,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            createdAt = System.currentTimeMillis()
        )
        db.tradeDao().insertTrade(trade)

        // 3. Notification
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = userId,
                title = "Trade Opened: $assetSymbol",
                message = "Opened ${direction.label} ($leverage x) with $amount USDT margin at $entryPrice",
                type = "TRADE"
            )
        )

        // 4. Audit Log
        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = userId,
                actorRole = "TRADER",
                action = "EXECUTE_TRADE",
                entityType = "TRADE",
                entityId = tradeId,
                previousValue = "availableBalance: ${wallet.availableBalance}",
                newValue = "availableBalance: ${updatedWallet.availableBalance}, trade: $tradeId",
                reason = "User initiated market order on $assetSymbol",
                clientInfo = "Apex Android Trader"
            )
        )

        Result.success(tradeId)
    }

    suspend fun closePosition(tradeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUserId = _currentUserId.value ?: "TRADER"
        closePositionInternal(tradeId, null, "Manual Trader Close", currentUserId)
    }

    private suspend fun closePositionInternal(
        tradeId: String,
        specifiedExitPrice: Double?,
        reason: String,
        actorId: String
    ): Result<Unit> {
        val trade = db.tradeDao().getTradeById(tradeId) ?: return Result.failure(Exception("Trade not found"))
        if (trade.status != "OPEN") return Result.failure(Exception("Trade is already closed"))

        val asset = _marketAssets.value.find { it.symbol == trade.assetSymbol }
        val exitPrice = specifiedExitPrice ?: asset?.price ?: trade.currentPrice
        val priceDiff = exitPrice - trade.entryPrice
        val isUp = trade.direction == "UP" || trade.direction == "BUY_LONG"
        val multiplier = if (isUp) 1.0 else -1.0
        val finalPnl = (priceDiff / trade.entryPrice) * trade.positionSize * multiplier
        val finalPnlPct = (finalPnl / trade.amount) * 100.0

        val wallet = db.walletDao().getWalletForUserSync(trade.userId) ?: return Result.failure(Exception("Wallet not found"))

        // Release margin and settle PnL
        val newAvailable = (wallet.availableBalance + trade.amount + finalPnl).coerceAtLeast(0.0)
        val newLocked = (wallet.lockedMargin - trade.amount).coerceAtLeast(0.0)
        val newTotal = (wallet.totalBalance + finalPnl).coerceAtLeast(0.0)
        val newTotalPnl = wallet.totalProfitLoss + finalPnl

        val updatedWallet = wallet.copy(
            totalBalance = newTotal,
            availableBalance = newAvailable,
            lockedMargin = newLocked,
            totalProfitLoss = newTotalPnl
        )
        db.walletDao().updateWallet(updatedWallet)

        // Close trade
        val closedTrade = trade.copy(
            exitPrice = exitPrice,
            currentPrice = exitPrice,
            status = "CLOSED",
            pnl = String.format(Locale.US, "%.2f", finalPnl).toDouble(),
            pnlPercentage = String.format(Locale.US, "%.2f", finalPnlPct).toDouble(),
            closedAt = System.currentTimeMillis(),
            closingReason = reason
        )
        db.tradeDao().updateTrade(closedTrade)

        // Notification
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = trade.userId,
                title = "Position Closed: ${trade.assetSymbol}",
                message = "Closed ${trade.direction} at $exitPrice with ${if (finalPnl >= 0) "+$" else "-$"}${abs(finalPnl)} PnL ($reason).",
                type = "TRADE"
            )
        )

        // Trigger on-screen real-time result notification with time
        val outcomeStr = if (finalPnl >= 0) "WIN" else "LOSS"
        val pnlStr = "${if (finalPnl >= 0) "+$" else "-$"}${String.format(Locale.US, "%.2f", abs(finalPnl))} USDT (${String.format(Locale.US, "%.1f", finalPnlPct)}%)"
        _latestSettlementAlert.value = TradeSettlementAlert(
            tradeId = trade.id,
            userId = trade.userId,
            username = trade.username,
            assetSymbol = trade.assetSymbol,
            outcome = outcomeStr,
            pnlText = pnlStr,
            formattedTime = SimpleDateFormat("hh:mm:ss a • dd MMM yyyy", Locale.US).format(Date()),
            reason = reason
        )

        // Audit Log
        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = actorId,
                actorRole = if (actorId.startsWith("ADM")) "ADMIN" else if (actorId == "SYSTEM") "SYSTEM" else "TRADER",
                action = "CLOSE_POSITION",
                entityType = "TRADE",
                entityId = tradeId,
                previousValue = "status: OPEN, margin: ${trade.amount}",
                newValue = "status: CLOSED, exitPrice: $exitPrice, pnl: $finalPnl",
                reason = reason,
                clientInfo = "Apex Trading Core"
            )
        )

        return Result.success(Unit)
    }

    // Submit user deposit request
    suspend fun submitDeposit(
        amount: Double,
        network: String,
        walletAddress: String,
        txHash: String,
        userNote: String?,
        screenshotData: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value ?: return@withContext Result.failure(Exception("Please log in to deposit"))
        val user = db.userDao().getUserByIdSync(uid) ?: return@withContext Result.failure(Exception("User not found"))
        val depositId = "DEP-${Random.nextInt(10000, 99999)}"

        val deposit = DepositEntity(
            id = depositId,
            userId = user.id,
            username = user.username,
            amount = amount,
            currency = "USDT",
            network = network,
            walletAddress = walletAddress,
            txHash = txHash,
            screenshotData = screenshotData ?: "RECEIPT_TX_${System.currentTimeMillis()}",
            status = "PENDING",
            createdAt = System.currentTimeMillis(),
            userNote = userNote
        )
        db.depositDao().insertDeposit(deposit)

        // Notify user
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = user.id,
                title = "Deposit Request Submitted",
                message = "Your deposit of $amount USDT via $network is pending admin review.",
                type = "DEPOSIT"
            )
        )

        // Audit log
        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = user.id,
                actorRole = "TRADER",
                action = "SUBMIT_DEPOSIT",
                entityType = "DEPOSIT",
                entityId = depositId,
                previousValue = "NONE",
                newValue = "amount: $amount, txHash: $txHash",
                reason = "User submitted deposit request with payment screenshot",
                clientInfo = "Apex Mobile Client"
            )
        )

        Result.success(depositId)
    }

    // Admin deposit approval
    suspend fun approveDeposit(depositId: String, adminId: String, note: String = "Blockchain TX confirmed"): Result<Unit> = withContext(Dispatchers.IO) {
        val deposit = db.depositDao().getDepositById(depositId) ?: return@withContext Result.failure(Exception("Deposit not found"))
        if (deposit.status != "PENDING") return@withContext Result.failure(Exception("Deposit is not pending"))

        val wallet = db.walletDao().getWalletForUserSync(deposit.userId) ?: return@withContext Result.failure(Exception("User wallet not found"))

        // Credit user balance
        val updatedWallet = wallet.copy(
            totalBalance = wallet.totalBalance + deposit.amount,
            availableBalance = wallet.availableBalance + deposit.amount,
            totalDeposited = wallet.totalDeposited + deposit.amount
        )
        db.walletDao().updateWallet(updatedWallet)

        // Update deposit status
        db.depositDao().updateDeposit(
            deposit.copy(
                status = "APPROVED",
                reviewedAt = System.currentTimeMillis(),
                reviewedBy = adminId
            )
        )

        // Notify user
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = deposit.userId,
                title = "Deposit Approved: +${deposit.amount} USDT",
                message = "Your deposit of ${deposit.amount} USDT (${deposit.network}) has been approved and credited to your wallet.",
                type = "DEPOSIT"
            )
        )

        // Audit Log
        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = adminId,
                actorRole = "FINANCE_ADMIN",
                action = "APPROVE_DEPOSIT",
                entityType = "DEPOSIT",
                entityId = depositId,
                previousValue = "status: PENDING, walletBalance: ${wallet.totalBalance}",
                newValue = "status: APPROVED, walletBalance: ${updatedWallet.totalBalance}",
                reason = note,
                clientInfo = "Apex Admin Workstation"
            )
        )

        Result.success(Unit)
    }

    // Admin deposit rejection
    suspend fun rejectDeposit(depositId: String, adminId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        val deposit = db.depositDao().getDepositById(depositId) ?: return@withContext Result.failure(Exception("Deposit not found"))
        if (deposit.status != "PENDING") return@withContext Result.failure(Exception("Deposit is not pending"))

        db.depositDao().updateDeposit(
            deposit.copy(
                status = "REJECTED",
                reviewedAt = System.currentTimeMillis(),
                reviewedBy = adminId,
                rejectionReason = reason
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = deposit.userId,
                title = "Deposit Rejected",
                message = "Your deposit of ${deposit.amount} USDT was rejected. Reason: $reason",
                type = "DEPOSIT"
            )
        )

        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = adminId,
                actorRole = "FINANCE_ADMIN",
                action = "REJECT_DEPOSIT",
                entityType = "DEPOSIT",
                entityId = depositId,
                previousValue = "status: PENDING",
                newValue = "status: REJECTED",
                reason = reason,
                clientInfo = "Apex Admin Workstation"
            )
        )

        Result.success(Unit)
    }

    // Submit user withdrawal request
    suspend fun submitWithdrawal(
        amount: Double,
        network: String,
        destinationAddress: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value ?: return@withContext Result.failure(Exception("Please log in to withdraw"))
        val user = db.userDao().getUserByIdSync(uid) ?: return@withContext Result.failure(Exception("User not found"))
        val wallet = db.walletDao().getWalletForUserSync(user.id) ?: return@withContext Result.failure(Exception("Wallet not found"))

        if (wallet.availableBalance < amount) {
            return@withContext Result.failure(Exception("Insufficient available balance for withdrawal. Available: ${wallet.availableBalance} USDT"))
        }

        val withdrawalId = "WTH-${Random.nextInt(10000, 99999)}"

        // Deduct from available balance immediately to prevent double spending
        val updatedWallet = wallet.copy(
            availableBalance = wallet.availableBalance - amount
        )
        db.walletDao().updateWallet(updatedWallet)

        val withdrawal = WithdrawalEntity(
            id = withdrawalId,
            userId = user.id,
            username = user.username,
            amount = amount,
            currency = "USDT",
            network = network,
            destinationAddress = destinationAddress,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        db.withdrawalDao().insertWithdrawal(withdrawal)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = user.id,
                title = "Withdrawal Request Received",
                message = "Your withdrawal request for $amount USDT to $destinationAddress is pending processing.",
                type = "WITHDRAWAL"
            )
        )

        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = user.id,
                actorRole = "TRADER",
                action = "SUBMIT_WITHDRAWAL",
                entityType = "WITHDRAWAL",
                entityId = withdrawalId,
                previousValue = "availableBalance: ${wallet.availableBalance}",
                newValue = "availableBalance: ${updatedWallet.availableBalance}, withdrawal: $withdrawalId",
                reason = "User submitted withdrawal request",
                clientInfo = "Apex Mobile Client"
            )
        )

        Result.success(withdrawalId)
    }

    // Admin process withdrawal
    suspend fun completeWithdrawal(withdrawalId: String, adminId: String, txHash: String, notes: String): Result<Unit> = withContext(Dispatchers.IO) {
        val w = db.withdrawalDao().getWithdrawalById(withdrawalId) ?: return@withContext Result.failure(Exception("Withdrawal not found"))
        if (w.status != "PENDING" && w.status != "PROCESSING") return@withContext Result.failure(Exception("Withdrawal cannot be processed in ${w.status} state"))

        val wallet = db.walletDao().getWalletForUserSync(w.userId) ?: return@withContext Result.failure(Exception("Wallet not found"))

        // Finalize balance deduction
        val updatedWallet = wallet.copy(
            totalBalance = wallet.totalBalance - w.amount,
            totalWithdrawn = wallet.totalWithdrawn + w.amount
        )
        db.walletDao().updateWallet(updatedWallet)

        db.withdrawalDao().updateWithdrawal(
            w.copy(
                status = "COMPLETED",
                processedAt = System.currentTimeMillis(),
                processedBy = adminId,
                txHash = txHash,
                adminNotes = notes
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = w.userId,
                title = "Withdrawal Completed: ${w.amount} USDT",
                message = "Your withdrawal of ${w.amount} USDT has been sent to ${w.destinationAddress}. TX: $txHash",
                type = "WITHDRAWAL"
            )
        )

        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = adminId,
                actorRole = "FINANCE_ADMIN",
                action = "COMPLETE_WITHDRAWAL",
                entityType = "WITHDRAWAL",
                entityId = withdrawalId,
                previousValue = "status: ${w.status}, totalBalance: ${wallet.totalBalance}",
                newValue = "status: COMPLETED, totalBalance: ${updatedWallet.totalBalance}, tx: $txHash",
                reason = notes,
                clientInfo = "Apex Admin Workstation"
            )
        )

        Result.success(Unit)
    }

    // Admin reject withdrawal (Refunds balance)
    suspend fun rejectWithdrawal(withdrawalId: String, adminId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        val w = db.withdrawalDao().getWithdrawalById(withdrawalId) ?: return@withContext Result.failure(Exception("Withdrawal not found"))
        if (w.status != "PENDING" && w.status != "PROCESSING") return@withContext Result.failure(Exception("Withdrawal not pending"))

        val wallet = db.walletDao().getWalletForUserSync(w.userId) ?: return@withContext Result.failure(Exception("Wallet not found"))

        // Refund available balance
        val updatedWallet = wallet.copy(
            availableBalance = wallet.availableBalance + w.amount
        )
        db.walletDao().updateWallet(updatedWallet)

        db.withdrawalDao().updateWithdrawal(
            w.copy(
                status = "REJECTED",
                processedAt = System.currentTimeMillis(),
                processedBy = adminId,
                adminNotes = "Rejected: $reason"
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = w.userId,
                title = "Withdrawal Rejected & Refunded",
                message = "Your withdrawal of ${w.amount} USDT was rejected and funds were refunded to your available balance. Reason: $reason",
                type = "WITHDRAWAL"
            )
        )

        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = adminId,
                actorRole = "FINANCE_ADMIN",
                action = "REJECT_WITHDRAWAL",
                entityType = "WITHDRAWAL",
                entityId = withdrawalId,
                previousValue = "status: ${w.status}, availableBalance: ${wallet.availableBalance}",
                newValue = "status: REJECTED, availableBalance: ${updatedWallet.availableBalance}",
                reason = reason,
                clientInfo = "Apex Admin Workstation"
            )
        )

        Result.success(Unit)
    }

    // Admin User Status Toggle
    suspend fun updateUserStatus(userId: String, newStatus: AccountStatus, adminId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = db.userDao().getUserByIdSync(userId) ?: return@withContext Result.failure(Exception("User not found"))
        val prev = user.accountStatus
        db.userDao().updateAccountStatus(userId, newStatus.name)

        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = adminId,
                actorRole = "SUPER_ADMIN",
                action = "UPDATE_USER_STATUS",
                entityType = "USER",
                entityId = userId,
                previousValue = "status: $prev",
                newValue = "status: ${newStatus.name}",
                reason = reason,
                clientInfo = "Apex Admin Workstation"
            )
        )

        Result.success(Unit)
    }

    // Transparent Financial Correction / Balance Adjustment
    suspend fun adjustUserBalance(
        targetUserId: String,
        amountChange: Double,
        adminId: String,
        mandatoryReason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (mandatoryReason.trim().length < 5) {
            return@withContext Result.failure(Exception("A thorough audit reason (minimum 5 chars) is mandatory for transparent financial adjustments."))
        }
        val wallet = db.walletDao().getWalletForUserSync(targetUserId) ?: return@withContext Result.failure(Exception("Target wallet not found"))

        val newTotal = (wallet.totalBalance + amountChange).coerceAtLeast(0.0)
        val newAvailable = (wallet.availableBalance + amountChange).coerceAtLeast(0.0)

        val updated = wallet.copy(
            totalBalance = newTotal,
            availableBalance = newAvailable
        )
        db.walletDao().updateWallet(updated)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = targetUserId,
                title = "Account Balance Adjustment",
                message = "An administrative adjustment of ${if (amountChange >= 0) "+$" else "-$"}${abs(amountChange)} USDT was applied. Reason: $mandatoryReason",
                type = "SYSTEM"
            )
        )

        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = adminId,
                actorRole = "FINANCE_ADMIN",
                action = "ADJUST_BALANCE",
                entityType = "WALLET",
                entityId = targetUserId,
                previousValue = "total: ${wallet.totalBalance}, available: ${wallet.availableBalance}",
                newValue = "total: $newTotal, available: $newAvailable, change: $amountChange",
                reason = mandatoryReason,
                clientInfo = "Apex Admin Workstation"
            )
        )

        Result.success(Unit)
    }

    // Admin Payment Method Management
    suspend fun savePaymentMethod(method: PaymentMethodEntity, adminId: String): Result<Unit> = withContext(Dispatchers.IO) {
        db.paymentMethodDao().insertPaymentMethod(method)

        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = adminId,
                actorRole = "SUPER_ADMIN",
                action = "SAVE_PAYMENT_METHOD",
                entityType = "PAYMENT_METHOD",
                entityId = method.id,
                previousValue = "N/A",
                newValue = "method: ${method.name}, network: ${method.network}, address: ${method.walletAddressOrAccount}, active: ${method.isActive}",
                reason = "Payment method updated/added in Admin settings",
                clientInfo = "MRG Company Portal"
            )
        )

        Result.success(Unit)
    }

    // MRG Company Admin: Direct Deposit Funds into User's ID
    suspend fun adminDepositToUser(targetUserId: String, amount: Double, adminId: String, note: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = db.userDao().getUserByIdSync(targetUserId) ?: return@withContext Result.failure(Exception("User not found with ID: $targetUserId"))
        val wallet = db.walletDao().getWalletForUserSync(targetUserId) ?: return@withContext Result.failure(Exception("Wallet not found for ID: $targetUserId"))

        val updatedWallet = wallet.copy(
            totalBalance = wallet.totalBalance + amount,
            availableBalance = wallet.availableBalance + amount,
            totalDeposited = wallet.totalDeposited + amount
        )
        db.walletDao().updateWallet(updatedWallet)

        val depositId = "DEP-MRG-${Random.nextInt(10000, 99999)}"
        val deposit = DepositEntity(
            id = depositId,
            userId = targetUserId,
            username = user.username,
            amount = amount,
            currency = "USDT",
            network = "MRG Company Direct Deposit",
            walletAddress = "MRG_DIRECT_CREDIT",
            txHash = "mrg_credit_${System.currentTimeMillis()}",
            screenshotData = "MRG_TREASURY_APPROVAL",
            status = "APPROVED",
            createdAt = System.currentTimeMillis(),
            reviewedAt = System.currentTimeMillis(),
            reviewedBy = adminId,
            userNote = note
        )
        db.depositDao().insertDeposit(deposit)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = targetUserId,
                title = "Funds Deposited: +$amount USDT",
                message = "MRG Company Admin ($adminId) deposited $amount USDT directly into your account ($note).",
                type = "DEPOSIT"
            )
        )

        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = adminId,
                actorRole = "SUPER_ADMIN",
                action = "ADMIN_DIRECT_DEPOSIT",
                entityType = "WALLET",
                entityId = targetUserId,
                previousValue = "balance: ${wallet.totalBalance}",
                newValue = "balance: ${updatedWallet.totalBalance}, added: +$amount",
                reason = note,
                clientInfo = "MRG Company Portal"
            )
        )

        Result.success(Unit)
    }

    // ApexTrade / MRG Admin: Force Trade WIN
    suspend fun adminSetTradeWin(tradeId: String, adminId: String, profitPct: Double? = null): Result<Unit> = withContext(Dispatchers.IO) {
        val trade = db.tradeDao().getTradeById(tradeId) ?: return@withContext Result.failure(Exception("Trade not found with ID: $tradeId"))
        if (trade.status != "OPEN") return@withContext Result.failure(Exception("Trade is already settled"))

        val actualProfitPct = profitPct ?: trade.profitPercentage
        val wallet = db.walletDao().getWalletForUserSync(trade.userId) ?: return@withContext Result.failure(Exception("Wallet not found for trader"))
        val profit = String.format(Locale.US, "%.2f", trade.amount * (actualProfitPct / 100.0)).toDouble()

        val updatedWallet = wallet.copy(
            totalBalance = wallet.totalBalance + profit,
            availableBalance = wallet.availableBalance + trade.amount + profit,
            lockedMargin = (wallet.lockedMargin - trade.amount).coerceAtLeast(0.0),
            totalProfitLoss = wallet.totalProfitLoss + profit
        )
        db.walletDao().updateWallet(updatedWallet)

        val closedTrade = trade.copy(
            status = "CLOSED",
            adminDecision = "WIN",
            pnl = profit,
            pnlPercentage = actualProfitPct,
            closedAt = System.currentTimeMillis(),
            closingReason = "ApexTrade Admin Win (+$profit USDT • ${actualProfitPct.toInt()}%)"
        )
        db.tradeDao().updateTrade(closedTrade)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = trade.userId,
                title = "Trade Settled: WIN (+$profit USDT)",
                message = "Congratulations! Your trade on ${trade.assetSymbol} [${trade.id}] was closed with a profit of $profit USDT (+${actualProfitPct.toInt()}%) at ${SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())}.",
                type = "TRADE"
            )
        )

        // Trigger on-screen real-time result notification with time
        _latestSettlementAlert.value = TradeSettlementAlert(
            tradeId = trade.id,
            userId = trade.userId,
            username = trade.username,
            assetSymbol = trade.assetSymbol,
            outcome = "WIN",
            pnlText = "+$profit USDT (+${actualProfitPct.toInt()}%)",
            formattedTime = SimpleDateFormat("hh:mm:ss a • dd MMM yyyy", Locale.US).format(Date()),
            reason = "Admin Confirmed WIN • Profit: +${actualProfitPct.toInt()}%"
        )

        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = adminId,
                actorRole = "SUPER_ADMIN",
                action = "ADMIN_SET_TRADE_WIN",
                entityType = "TRADE",
                entityId = tradeId,
                previousValue = "status: OPEN, margin: ${trade.amount}",
                newValue = "status: CLOSED (WIN), profit: +$profit USDT, user: ${trade.username}",
                reason = "MRG Company Administrator confirmed trade as WIN",
                clientInfo = "MRG Company Portal"
            )
        )

        Result.success(Unit)
    }

    // MRG Company Admin: Force Trade LOSS
    suspend fun adminSetTradeLoss(tradeId: String, adminId: String, lossReason: String = "Admin Market Decision"): Result<Unit> = withContext(Dispatchers.IO) {
        val trade = db.tradeDao().getTradeById(tradeId) ?: return@withContext Result.failure(Exception("Trade not found with ID: $tradeId"))
        if (trade.status != "OPEN") return@withContext Result.failure(Exception("Trade is already settled"))

        val wallet = db.walletDao().getWalletForUserSync(trade.userId) ?: return@withContext Result.failure(Exception("Wallet not found for trader"))
        val loss = trade.amount

        val updatedWallet = wallet.copy(
            totalBalance = (wallet.totalBalance - loss).coerceAtLeast(0.0),
            lockedMargin = (wallet.lockedMargin - trade.amount).coerceAtLeast(0.0),
            totalProfitLoss = wallet.totalProfitLoss - loss
        )
        db.walletDao().updateWallet(updatedWallet)

        val closedTrade = trade.copy(
            status = "CLOSED",
            adminDecision = "LOSS",
            pnl = -loss,
            pnlPercentage = -100.0,
            closedAt = System.currentTimeMillis(),
            closingReason = "MRG Company Admin Settle: LOSS ($lossReason)"
        )
        db.tradeDao().updateTrade(closedTrade)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = trade.userId,
                title = "Trade Closed: LOSS (-$loss USDT)",
                message = "Your trade on ${trade.assetSymbol} [${trade.id}] closed as a Loss at ${SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())}. Margin of $loss USDT was absorbed.",
                type = "TRADE"
            )
        )

        // Trigger on-screen real-time result notification with time
        _latestSettlementAlert.value = TradeSettlementAlert(
            tradeId = trade.id,
            userId = trade.userId,
            username = trade.username,
            assetSymbol = trade.assetSymbol,
            outcome = "LOSS",
            pnlText = "-$loss USDT (-100%)",
            formattedTime = SimpleDateFormat("hh:mm:ss a • dd MMM yyyy", Locale.US).format(Date()),
            reason = lossReason
        )

        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = adminId,
                actorRole = "SUPER_ADMIN",
                action = "ADMIN_SET_TRADE_LOSS",
                entityType = "TRADE",
                entityId = tradeId,
                previousValue = "status: OPEN, margin: ${trade.amount}",
                newValue = "status: CLOSED (LOSS), loss: -$loss USDT, user: ${trade.username}",
                reason = lossReason,
                clientInfo = "MRG Company Portal"
            )
        )

        Result.success(Unit)
    }

    // Google / Email Sign-In with 0.00 Balance guarantee for new users
    suspend fun loginOrRegisterWithGoogle(email: String, displayName: String): Result<String> = withContext(Dispatchers.IO) {
        val existing = db.userDao().getUserByEmail(email)
        if (existing != null) {
            _currentUserId.value = existing.id
            return@withContext Result.success(existing.id)
        }

        val newId = "USR-${Random.nextInt(10000, 99999)}"
        val user = UserEntity(
            id = newId,
            username = displayName.ifBlank { "Trader_${Random.nextInt(100, 999)}" },
            email = email,
            phone = if (email == "ffurqanraees@gmail.com") "+92 300 1234567" else "+1 (555) 000-0000",
            role = "TRADER",
            accountStatus = "ACTIVE",
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis(),
            isVerified = true
        )
        db.userDao().insertUser(user)

        // Strict 0.00 Balance until deposit
        val initialWallet = WalletEntity(
            userId = newId,
            totalBalance = 0.00,
            availableBalance = 0.00,
            lockedMargin = 0.00,
            totalDeposited = 0.00,
            totalWithdrawn = 0.00,
            totalProfitLoss = 0.00
        )
        db.walletDao().insertWallet(initialWallet)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = newId,
                title = "Welcome to ApexTrade",
                message = "Your account is active with 0.00 USDT balance. Please deposit funds via TRON (TRC20) to fund your account and start trading.",
                type = "SYSTEM"
            )
        )

        _currentUserId.value = newId
        Result.success(newId)
    }

    suspend fun updateUserProfile(userId: String, username: String, email: String, phone: String): Result<Unit> = withContext(Dispatchers.IO) {
        db.userDao().updateProfileDetails(userId, username, email, phone)
        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = userId,
                actorRole = "TRADER",
                action = "UPDATE_PROFILE",
                entityType = "USER",
                entityId = userId,
                previousValue = "N/A",
                newValue = "username: $username, email: $email, phone: $phone",
                reason = "User updated profile details in settings",
                clientInfo = "Apex Mobile Client"
            )
        )
        Result.success(Unit)
    }

    suspend fun toggleTwoFactor(userId: String, enabled: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        db.userDao().updateTwoFactor(userId, enabled)
        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = userId,
                actorRole = "TRADER",
                action = "TOGGLE_2FA",
                entityType = "USER",
                entityId = userId,
                previousValue = (!enabled).toString(),
                newValue = enabled.toString(),
                reason = "User modified two-factor authentication state",
                clientInfo = "Apex Mobile Client"
            )
        )
        Result.success(Unit)
    }

    suspend fun markNotificationsRead() = withContext(Dispatchers.IO) {
        _currentUserId.value?.let { uid ->
            db.notificationDao().markAllAsRead(uid)
        }
    }

    // Database JSON backup / export generator
    suspend fun exportDatabaseBackupJson(): String = withContext(Dispatchers.IO) {
        val users = db.userDao().getAllUsers().first()
        val wallets = db.walletDao().getAllWallets().first()
        val trades = db.tradeDao().getAllTrades().first()
        val deposits = db.depositDao().getAllDeposits().first()
        val withdrawals = db.withdrawalDao().getAllWithdrawals().first()
        val pms = db.paymentMethodDao().getAllPaymentMethods().first()
        val logs = db.auditLogDao().getAllAuditLogs().first()

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"exportedAt\": \"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\",\n")
        sb.append("  \"platform\": \"ApexTrade Centralized Database\",\n")
        sb.append("  \"totalUsers\": ${users.size},\n")
        sb.append("  \"totalTrades\": ${trades.size},\n")
        sb.append("  \"totalDeposits\": ${deposits.size},\n")
        sb.append("  \"totalWithdrawals\": ${withdrawals.size},\n")
        sb.append("  \"totalAuditLogs\": ${logs.size},\n")
        sb.append("  \"auditLogsSummary\": [\n")
        logs.take(15).forEachIndexed { index, l ->
            sb.append("    {\"id\": \"${l.id}\", \"actor\": \"${l.actorId}\", \"action\": \"${l.action}\", \"reason\": \"${l.reason}\"}${if (index < 14 && index < logs.size - 1) "," else ""}\n")
        }
        sb.append("  ]\n")
        sb.append("}")
        sb.toString()
    }
}

data class TradeSettlementAlert(
    val tradeId: String,
    val userId: String,
    val username: String,
    val assetSymbol: String,
    val outcome: String, // "WIN" or "LOSS"
    val pnlText: String,
    val formattedTime: String,
    val reason: String
)

