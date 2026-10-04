package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        WalletEntity::class,
        TradeEntity::class,
        DepositEntity::class,
        WithdrawalEntity::class,
        PaymentMethodEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun tradeDao(): TradeDao
    abstract fun depositDao(): DepositDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun notificationDao(): NotificationDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apextrade_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (database.userDao().getUserByIdSync("Fadi Raees") == null) {
                            database.userDao().insertUser(
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
                            database.walletDao().insertWallet(
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
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val now = System.currentTimeMillis()

            // 1. Seed Payment Methods (Crucial TRON TRC20 address from prompt)
            val paymentMethods = listOf(
                PaymentMethodEntity(
                    id = "PM-TRC20",
                    name = "Tether USDT",
                    network = "TRON (TRC20)",
                    walletAddressOrAccount = "TB5vxHpnn5mq8TVvcCA4JTLdcLfsiBK3ZZ",
                    holderName = "Apex Trade Custody TRC20",
                    instructions = "Send only USDT via TRON (TRC20) network. Funds arrive after 1 blockchain confirmation. Minimum 50 USDT.",
                    isActive = true,
                    isCrypto = true,
                    minDeposit = 50.0,
                    maxDeposit = 500000.0
                ),
                PaymentMethodEntity(
                    id = "PM-BTC",
                    name = "Bitcoin",
                    network = "BTC (Bitcoin Native)",
                    walletAddressOrAccount = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
                    holderName = "Apex Trade Treasury BTC",
                    instructions = "Send Bitcoin Native (SegWit). 2 blockchain confirmations required.",
                    isActive = true,
                    isCrypto = true,
                    minDeposit = 100.0,
                    maxDeposit = 1000000.0
                ),
                PaymentMethodEntity(
                    id = "PM-ETH",
                    name = "Ethereum / USDT",
                    network = "Ethereum (ERC20)",
                    walletAddressOrAccount = "0x71C8360f3E4249a5b48B6999a3994326577D5d46",
                    holderName = "Apex Trade ERC20 Treasury",
                    instructions = "Send ERC20 USDT or ETH. 12 block confirmations required.",
                    isActive = true,
                    isCrypto = true,
                    minDeposit = 100.0,
                    maxDeposit = 500000.0
                ),
                PaymentMethodEntity(
                    id = "PM-BANK",
                    name = "Bank Wire Transfer",
                    network = "SWIFT / SEPA Wire",
                    walletAddressOrAccount = "IBAN: GB82APEX90812384918239 / SWIFT: APEXGB2L",
                    holderName = "Apex Global Markets Ltd.",
                    instructions = "Include your User ID in the wire reference. Processing time: 1-2 business days.",
                    isActive = true,
                    isCrypto = false,
                    minDeposit = 500.0,
                    maxDeposit = 2000000.0
                )
            )
            db.paymentMethodDao().insertPaymentMethods(paymentMethods)

            // 2. Seed Users (MRG Company Super Admin, Finance Admin, Support Admin, Readonly Admin, and Traders)
            val users = listOf(
                UserEntity(
                    id = "Fadi Raees",
                    username = "Fadi Raees",
                    email = "admin@mrgcompany.io",
                    phone = "+1 (800) 555-0100",
                    role = "SUPER_ADMIN",
                    accountStatus = "ACTIVE",
                    createdAt = now - 90L * 86400000L,
                    lastLoginAt = now - 1800000L,
                    isVerified = true,
                    twoFactorEnabled = true,
                    internalNotes = "MRG Company Master Administrator. Password: Furqan@786"
                ),
                UserEntity(
                    id = "ADM-002",
                    username = "finance_lead",
                    email = "finance@apextrade.io",
                    phone = "+1 (800) 555-0101",
                    role = "FINANCE_ADMIN",
                    accountStatus = "ACTIVE",
                    createdAt = now - 60L * 86400000L,
                    lastLoginAt = now - 3600000L,
                    isVerified = true,
                    twoFactorEnabled = true,
                    internalNotes = "Treasury & Finance lead. Authorized for deposits and withdrawals."
                ),
                UserEntity(
                    id = "ADM-003",
                    username = "support_rep",
                    email = "support@apextrade.io",
                    phone = "+1 (800) 555-0102",
                    role = "SUPPORT_ADMIN",
                    accountStatus = "ACTIVE",
                    createdAt = now - 45L * 86400000L,
                    lastLoginAt = now - 7200000L,
                    isVerified = true,
                    twoFactorEnabled = false,
                    internalNotes = "Customer support lead."
                ),
                UserEntity(
                    id = "ADM-004",
                    username = "compliance_auditor",
                    email = "compliance@apextrade.io",
                    phone = "+1 (800) 555-0103",
                    role = "READONLY_ADMIN",
                    accountStatus = "ACTIVE",
                    createdAt = now - 30L * 86400000L,
                    lastLoginAt = now - 14400000L,
                    isVerified = true,
                    twoFactorEnabled = true,
                    internalNotes = "External regulatory compliance auditor. Read-only access."
                ),
                UserEntity(
                    id = "USR-1082",
                    username = "trader_john",
                    email = "john.trader@example.com",
                    phone = "+1 (555) 234-8901",
                    role = "TRADER",
                    accountStatus = "ACTIVE",
                    createdAt = now - 20L * 86400000L,
                    lastLoginAt = now - 600000L,
                    isVerified = true,
                    twoFactorEnabled = true,
                    internalNotes = "VIP Level 2 active futures & spot trader."
                ),
                UserEntity(
                    id = "USR-2094",
                    username = "crypto_sarah",
                    email = "sarah.chen@example.com",
                    phone = "+1 (555) 876-5432",
                    role = "TRADER",
                    accountStatus = "ACTIVE",
                    createdAt = now - 15L * 86400000L,
                    lastLoginAt = now - 1200000L,
                    isVerified = true,
                    twoFactorEnabled = false,
                    internalNotes = "High volume crypto scalper."
                ),
                UserEntity(
                    id = "USR-3910",
                    username = "marcus_fx",
                    email = "marcus.k@example.com",
                    phone = "+44 20 7946 0912",
                    role = "TRADER",
                    accountStatus = "ACTIVE",
                    createdAt = now - 8L * 86400000L,
                    lastLoginAt = now - 3600000L,
                    isVerified = true,
                    twoFactorEnabled = false,
                    internalNotes = "Forex & Commodities trader."
                ),
                UserEntity(
                    id = "USR-5001",
                    username = "new_user_zero",
                    email = "ali.trader@example.com",
                    phone = "+1 (555) 019-8822",
                    role = "TRADER",
                    accountStatus = "ACTIVE",
                    createdAt = now - 1800000L,
                    lastLoginAt = now - 600000L,
                    isVerified = true,
                    twoFactorEnabled = false,
                    internalNotes = "New user account. Balance is 0.00 until initial deposit."
                )
            )
            db.userDao().insertUsers(users)

            // 3. Seed Wallets
            val wallets = listOf(
                WalletEntity(
                    userId = "USR-1082",
                    totalBalance = 15450.00,
                    availableBalance = 12450.00,
                    lockedMargin = 3000.00,
                    totalDeposited = 16000.00,
                    totalWithdrawn = 2000.00,
                    totalProfitLoss = 1450.00
                ),
                WalletEntity(
                    userId = "USR-2094",
                    totalBalance = 28200.00,
                    availableBalance = 24200.00,
                    lockedMargin = 4000.00,
                    totalDeposited = 25000.00,
                    totalWithdrawn = 0.00,
                    totalProfitLoss = 3200.00
                ),
                WalletEntity(
                    userId = "USR-3910",
                    totalBalance = 5200.00,
                    availableBalance = 5200.00,
                    lockedMargin = 0.00,
                    totalDeposited = 5000.00,
                    totalWithdrawn = 0.00,
                    totalProfitLoss = 200.00
                ),
                WalletEntity(
                    userId = "USR-5001",
                    totalBalance = 0.00, // 0.00 balance until deposit
                    availableBalance = 0.00,
                    lockedMargin = 0.00,
                    totalDeposited = 0.00,
                    totalWithdrawn = 0.00,
                    totalProfitLoss = 0.00
                ),
                WalletEntity(
                    userId = "Fadi Raees",
                    totalBalance = 100000.00,
                    availableBalance = 100000.00,
                    lockedMargin = 0.00,
                    totalDeposited = 100000.00,
                    totalWithdrawn = 0.00,
                    totalProfitLoss = 0.00
                )
            )
            wallets.forEach { db.walletDao().insertWallet(it) }

            // 4. Seed Open & Closed Trades
            val trades = listOf(
                TradeEntity(
                    id = "TRD-8401",
                    userId = "USR-1082",
                    username = "trader_john",
                    assetSymbol = "BTC/USDT",
                    direction = "BUY_LONG",
                    amount = 2000.00,
                    leverage = 10,
                    positionSize = 20000.00,
                    entryPrice = 66500.00,
                    currentPrice = 67450.00,
                    status = "OPEN",
                    durationSeconds = 3600,
                    pnl = 285.71,
                    pnlPercentage = 14.28,
                    stopLoss = 64800.00,
                    takeProfit = 70000.00,
                    createdAt = now - 60000L
                ),
                TradeEntity(
                    id = "TRD-8402",
                    userId = "USR-1082",
                    username = "trader_john",
                    assetSymbol = "ETH/USDT",
                    direction = "BUY_LONG",
                    amount = 1000.00,
                    leverage = 5,
                    positionSize = 5000.00,
                    entryPrice = 3450.00,
                    currentPrice = 3520.00,
                    status = "OPEN",
                    durationSeconds = 3600,
                    pnl = 101.45,
                    pnlPercentage = 10.14,
                    stopLoss = 3350.00,
                    takeProfit = 3700.00,
                    createdAt = now - 120000L
                ),
                TradeEntity(
                    id = "TRD-8390",
                    userId = "USR-1082",
                    username = "trader_john",
                    assetSymbol = "SOL/USDT",
                    direction = "BUY_LONG",
                    amount = 1500.00,
                    leverage = 10,
                    positionSize = 15000.00,
                    entryPrice = 142.50,
                    currentPrice = 152.00,
                    exitPrice = 152.00,
                    status = "CLOSED",
                    pnl = 1000.00,
                    pnlPercentage = 66.67,
                    createdAt = now - 86400000L * 2,
                    closedAt = now - 86400000L,
                    closingReason = "Take Profit Target Achieved"
                ),
                TradeEntity(
                    id = "TRD-8375",
                    userId = "USR-2094",
                    username = "crypto_sarah",
                    assetSymbol = "BTC/USDT",
                    direction = "SELL_SHORT",
                    amount = 4000.00,
                    leverage = 20,
                    positionSize = 80000.00,
                    entryPrice = 67800.00,
                    currentPrice = 67450.00,
                    status = "OPEN",
                    pnl = 412.98,
                    pnlPercentage = 10.32,
                    createdAt = now - 3600000L
                )
            )
            trades.forEach { db.tradeDao().insertTrade(it) }

            // 5. Seed Deposits (Crucial TRON TRC20 address from user requirements)
            val deposits = listOf(
                DepositEntity(
                    id = "DEP-9021",
                    userId = "USR-1082",
                    username = "trader_john",
                    amount = 2500.00,
                    currency = "USDT",
                    network = "TRON (TRC20)",
                    walletAddress = "TB5vxHpnn5mq8TVvcCA4JTLdcLfsiBK3ZZ",
                    txHash = "4f8a9d1c7e3b5a2f8c6e0b4d9a1f7e3c5a2b8d6f0c4e8a2b6d0e4f8a9c2b5d7e",
                    screenshotData = "RECEIPT_TX_TRC20_2500USDT",
                    status = "PENDING",
                    createdAt = now - 900000L, // 15 mins ago
                    userNote = "Deposited 2,500 USDT from TrustWallet. Please confirm."
                ),
                DepositEntity(
                    id = "DEP-8940",
                    userId = "USR-2094",
                    username = "crypto_sarah",
                    amount = 5000.00,
                    currency = "USDT",
                    network = "TRON (TRC20)",
                    walletAddress = "TB5vxHpnn5mq8TVvcCA4JTLdcLfsiBK3ZZ",
                    txHash = "9a2b4c6e8f0a2d4b6e8f0a2c4e6a8b0d2e4f6a8b0c2e4d6f8a0b2c4e6f8a0b2c",
                    screenshotData = "RECEIPT_TX_TRC20_5000USDT",
                    status = "PENDING",
                    createdAt = now - 2700000L,
                    userNote = "Account funding round 2."
                ),
                DepositEntity(
                    id = "DEP-8720",
                    userId = "USR-1082",
                    username = "trader_john",
                    amount = 10000.00,
                    currency = "USDT",
                    network = "TRON (TRC20)",
                    walletAddress = "TB5vxHpnn5mq8TVvcCA4JTLdcLfsiBK3ZZ",
                    txHash = "7e3b5a2f8c6e0b4d9a1f7e3c5a2b8d6f0c4e8a2b6d0e4f8a9c2b5d7e4f8a9d1c",
                    screenshotData = "RECEIPT_TX_TRC20_10000USDT",
                    status = "APPROVED",
                    createdAt = now - 86400000L * 5,
                    reviewedAt = now - 86400000L * 5 + 600000L,
                    reviewedBy = "ADM-002",
                    userNote = "Initial account balance funding"
                )
            )
            deposits.forEach { db.depositDao().insertDeposit(it) }

            // 6. Seed Withdrawals
            val withdrawals = listOf(
                WithdrawalEntity(
                    id = "WTH-5102",
                    userId = "USR-1082",
                    username = "trader_john",
                    amount = 1200.00,
                    currency = "USDT",
                    network = "TRON (TRC20)",
                    destinationAddress = "TYDzsYUE28p2a5vP31f2m4a1v5B8x9Q3rT",
                    status = "PENDING",
                    createdAt = now - 1800000L,
                    adminNotes = "Pending review by finance desk"
                ),
                WithdrawalEntity(
                    id = "WTH-4908",
                    userId = "USR-1082",
                    username = "trader_john",
                    amount = 2000.00,
                    currency = "USDT",
                    network = "TRON (TRC20)",
                    destinationAddress = "TYDzsYUE28p2a5vP31f2m4a1v5B8x9Q3rT",
                    status = "COMPLETED",
                    createdAt = now - 86400000L * 3,
                    processedAt = now - 86400000L * 3 + 1200000L,
                    processedBy = "ADM-002",
                    txHash = "2d4b6e8f0a2c4e6a8b0d2e4f6a8b0c2e4d6f8a0b2c4e6f8a0b2c9a2b4c6e8f0a",
                    adminNotes = "Processed and verified on TRON network"
                )
            )
            withdrawals.forEach { db.withdrawalDao().insertWithdrawal(it) }

            // 7. Seed Notifications
            val notifications = listOf(
                NotificationEntity(
                    id = "NOTIF-01",
                    userId = "USR-1082",
                    title = "Deposit Under Review",
                    message = "Your deposit of 2,500.00 USDT (TRON TRC20) is being verified by the treasury desk.",
                    type = "DEPOSIT",
                    isRead = false,
                    createdAt = now - 900000L
                ),
                NotificationEntity(
                    id = "NOTIF-02",
                    userId = "USR-1082",
                    title = "Position Opened: BTC/USDT",
                    message = "Successfully opened 10x Buy/Long on BTC/USDT at \$66,500.00.",
                    type = "TRADE",
                    isRead = true,
                    createdAt = now - 18000000L
                ),
                NotificationEntity(
                    id = "NOTIF-03",
                    userId = "ALL",
                    title = "System Security Notice",
                    message = "TRC20 fast deposit processing is active. Average approval time is under 5 minutes.",
                    type = "SYSTEM",
                    isRead = false,
                    createdAt = now - 36000000L
                )
            )
            notifications.forEach { db.notificationDao().insertNotification(it) }

            // 8. Seed Audit Logs
            val auditLogs = listOf(
                AuditLogEntity(
                    id = "AUD-1001",
                    actorId = "ADM-002",
                    actorRole = "FINANCE_ADMIN",
                    action = "APPROVE_DEPOSIT",
                    entityType = "DEPOSIT",
                    entityId = "DEP-8720",
                    previousValue = "status: PENDING",
                    newValue = "status: APPROVED, balance +10000.00 USDT",
                    reason = "Verified on TRONSCAN blockchain explorer, 25 confirmations.",
                    clientInfo = "Apex Admin Portal / IP 192.168.1.10",
                    timestamp = now - 86400000L * 5 + 600000L
                ),
                AuditLogEntity(
                    id = "AUD-1002",
                    actorId = "USR-1082",
                    actorRole = "TRADER",
                    action = "EXECUTE_TRADE",
                    entityType = "TRADE",
                    entityId = "TRD-8401",
                    previousValue = "availableBalance: 14450.00",
                    newValue = "margin: 2000.00, availableBalance: 12450.00",
                    reason = "User opened BUY_LONG 10x on BTC/USDT",
                    clientInfo = "Apex Android Mobile v1.0.0 / IP 172.56.21.9",
                    timestamp = now - 18000000L
                ),
                AuditLogEntity(
                    id = "AUD-1003",
                    actorId = "ADM-001",
                    actorRole = "SUPER_ADMIN",
                    action = "UPDATE_PAYMENT_METHOD",
                    entityType = "PAYMENT_METHOD",
                    entityId = "PM-TRC20",
                    previousValue = "wallet: TB5vxHpnn5mq8TVvcCA4JTLdcLfsiBK3ZZ",
                    newValue = "status: ACTIVE, minDeposit: 50.00",
                    reason = "Verified TRC20 cold storage multi-signature compliance",
                    clientInfo = "Apex Admin Workstation / IP 10.0.0.1",
                    timestamp = now - 86400000L * 10
                )
            )
            auditLogs.forEach { db.auditLogDao().insertAuditLog(it) }
        }
    }
}
