package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // USR-1082, ADM-001
    val username: String,
    val email: String,
    val phone: String,
    val role: String, // from UserRole
    val accountStatus: String, // from AccountStatus
    val createdAt: Long,
    val lastLoginAt: Long,
    val isVerified: Boolean = true,
    val twoFactorEnabled: Boolean = false,
    val internalNotes: String? = null
)

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val userId: String,
    val totalBalance: Double,
    val availableBalance: Double,
    val lockedMargin: Double,
    val totalDeposited: Double,
    val totalWithdrawn: Double,
    val totalProfitLoss: Double
)

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey val id: String, // Tracking ID e.g. TRD-98124
    val userId: String,
    val username: String = "Trader", // User Name
    val assetSymbol: String,
    val direction: String, // BUY_LONG, SELL_SHORT
    val amount: Double, // Initial margin (USDT)
    val leverage: Int, // 1 to 50x
    val positionSize: Double, // amount * leverage
    val entryPrice: Double,
    val currentPrice: Double,
    val exitPrice: Double? = null,
    val status: String, // OPEN, CLOSED, LIQUIDATED
    val adminDecision: String? = null, // "WIN" or "LOSS"
    val durationSeconds: Int = 60, // Trade duration set by user (e.g. 30s, 60s, 120s, 300s)
    val profitPercentage: Double = 85.0, // Target profit percentage selected by user (10% to 100%)
    val pnl: Double = 0.0,
    val pnlPercentage: Double = 0.0,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val closingReason: String? = null
)

@Entity(tableName = "deposits")
data class DepositEntity(
    @PrimaryKey val id: String, // DEP-49102
    val userId: String,
    val username: String,
    val amount: Double,
    val currency: String = "USDT",
    val network: String = "TRON (TRC20)",
    val walletAddress: String = "TB5vxHpnn5mq8TVvcCA4JTLdcLfsiBK3ZZ",
    val txHash: String,
    val screenshotData: String? = null, // Mock receipt image / base64 / token
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val reviewedBy: String? = null,
    val rejectionReason: String? = null,
    val userNote: String? = null
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey val id: String, // WTH-38291
    val userId: String,
    val username: String,
    val amount: Double,
    val currency: String = "USDT",
    val network: String = "TRON (TRC20)",
    val destinationAddress: String,
    val status: String = "PENDING", // PENDING, PROCESSING, COMPLETED, REJECTED
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val processedBy: String? = null,
    val txHash: String? = null,
    val adminNotes: String? = null
)

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val network: String,
    val walletAddressOrAccount: String,
    val holderName: String,
    val instructions: String,
    val isActive: Boolean = true,
    val isCrypto: Boolean = true,
    val minDeposit: Double = 50.0,
    val maxDeposit: Double = 100000.0
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String, // Specific user ID or "ALL"
    val title: String,
    val message: String,
    val type: String, // DEPOSIT, WITHDRAWAL, TRADE, SECURITY, SYSTEM
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val actorId: String,
    val actorRole: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val previousValue: String,
    val newValue: String,
    val reason: String,
    val clientInfo: String,
    val timestamp: Long = System.currentTimeMillis()
)
