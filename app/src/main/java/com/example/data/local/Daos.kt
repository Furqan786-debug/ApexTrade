package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersSync(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSync(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("UPDATE users SET accountStatus = :status WHERE id = :id")
    suspend fun updateAccountStatus(id: String, status: String)

    @Query("UPDATE users SET internalNotes = :notes WHERE id = :id")
    suspend fun updateInternalNotes(id: String, notes: String)

    @Query("UPDATE users SET username = :username, email = :email, phone = :phone WHERE id = :id")
    suspend fun updateProfileDetails(id: String, username: String, email: String, phone: String)

    @Query("UPDATE users SET twoFactorEnabled = :enabled WHERE id = :id")
    suspend fun updateTwoFactor(id: String, enabled: Boolean)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE userId = :userId")
    fun getWalletForUser(userId: String): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE userId = :userId")
    suspend fun getWalletForUserSync(userId: String): WalletEntity?

    @Query("SELECT * FROM wallets")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)
}

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades ORDER BY createdAt DESC")
    fun getAllTrades(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTradesForUser(userId: String): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE userId = :userId AND status = 'OPEN' ORDER BY createdAt DESC")
    fun getOpenTradesForUser(userId: String): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE status = 'OPEN'")
    suspend fun getOpenTradesSync(): List<TradeEntity>

    @Query("SELECT * FROM trades WHERE id = :id")
    suspend fun getTradeById(id: String): TradeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity)

    @Update
    suspend fun updateTrade(trade: TradeEntity)
}

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits ORDER BY createdAt DESC")
    fun getAllDeposits(): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getDepositsForUser(userId: String): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingDeposits(): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE id = :id")
    suspend fun getDepositById(id: String): DepositEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositEntity)

    @Update
    suspend fun updateDeposit(deposit: DepositEntity)
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals ORDER BY createdAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWithdrawalsForUser(userId: String): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE id = :id")
    suspend fun getWithdrawalById(id: String): WithdrawalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)
}

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods")
    fun getAllPaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE isActive = 1")
    fun getActivePaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE id = :id")
    suspend fun getPaymentMethodById(id: String): PaymentMethodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(pm: PaymentMethodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(pms: List<PaymentMethodEntity>)

    @Update
    suspend fun updatePaymentMethod(pm: PaymentMethodEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId = 'ALL' ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR userId = 'ALL'")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}
