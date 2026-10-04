package com.example.data.model

enum class UserRole(val displayName: String, val level: Int) {
    SUPER_ADMIN("Super Admin", 4),
    FINANCE_ADMIN("Finance Admin", 3),
    SUPPORT_ADMIN("Support Admin", 2),
    READONLY_ADMIN("Read-only Admin", 1),
    TRADER("Trader", 0);

    val isAdmin: Boolean get() = this != TRADER
    val canApproveFinances: Boolean get() = this == SUPER_ADMIN || this == FINANCE_ADMIN
    val canManageUsers: Boolean get() = this == SUPER_ADMIN || this == SUPPORT_ADMIN
    val canModifySettings: Boolean get() = this == SUPER_ADMIN
}

enum class AccountStatus(val displayName: String) {
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    PENDING_VERIFICATION("Pending Verification")
}

enum class TradeDirection(val label: String) {
    UP("UP"),
    DOWN("DOWN"),
    BUY_LONG("UP"),
    SELL_SHORT("DOWN")
}

enum class TradeStatus(val label: String) {
    OPEN("Open"),
    CLOSED("Closed"),
    LIQUIDATED("Liquidated")
}

enum class TransactionStatus(val label: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    APPROVED("Approved"),
    COMPLETED("Completed"),
    REJECTED("Rejected")
}

data class MarketAsset(
    val symbol: String,
    val name: String,
    val price: Double,
    val change24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: Double,
    val sparkline: List<Double> = emptyList(),
    val isCrypto: Boolean = true
)
