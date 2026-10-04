package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DepositEntity
import com.example.data.local.TradeEntity
import com.example.data.local.UserEntity
import com.example.data.local.WithdrawalEntity
import com.example.ui.components.FintechMetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.TradingViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun AdminDashboardScreen(
    viewModel: TradingViewModel,
    currentUser: UserEntity?,
    users: List<UserEntity>,
    trades: List<TradeEntity>,
    deposits: List<DepositEntity>,
    pendingDeposits: List<DepositEntity>,
    withdrawals: List<WithdrawalEntity>,
    pendingWithdrawals: List<WithdrawalEntity>,
    modifier: Modifier = Modifier
) {
    val totalUsers = users.size
    val activeUsers = users.count { it.accountStatus == "ACTIVE" }
    val totalDeposited = deposits.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val pendingDepositSum = pendingDeposits.sumOf { it.amount }
    val totalWithdrawn = withdrawals.filter { it.status == "COMPLETED" }.sumOf { it.amount }
    val pendingWithdrawalSum = pendingWithdrawals.sumOf { it.amount }
    val totalVolume = trades.sumOf { it.positionSize }
    val openTradesCount = trades.count { it.status == "OPEN" }

    val currentTime by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(1000)
            value = System.currentTimeMillis()
        }
    }

    var showDirectDepositModal by remember { mutableStateOf(false) }
    var targetUserIdInput by remember { mutableStateOf(users.firstOrNull { it.role == "TRADER" }?.id ?: "USR-1082") }
    var depositAmountInput by remember { mutableStateOf("500") }
    var depositNoteInput by remember { mutableStateOf("MRG Company Direct Account Funding") }

    // Direct Deposit into User ID Modal
    if (showDirectDepositModal) {
        AlertDialog(
            onDismissRequest = { showDirectDepositModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = TronGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Deposit Funds into User ID",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Add funds directly into any user's account from MRG Company Admin backend:",
                        color = Slate400,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = targetUserIdInput,
                        onValueChange = { targetUserIdInput = it },
                        label = { Text("Target User ID (e.g. USR-1082)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick User ID Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        users.filter { it.role == "TRADER" }.take(3).forEach { u ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (targetUserIdInput == u.id) TradeGreen.copy(alpha = 0.2f) else ObsidianSurfaceVariant,
                                modifier = Modifier
                                    .clickable { targetUserIdInput = u.id }
                            ) {
                                Text(
                                    text = "${u.id} (${u.username})",
                                    color = if (targetUserIdInput == u.id) TradeGreen else Slate400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = depositAmountInput,
                        onValueChange = { depositAmountInput = it },
                        label = { Text("Deposit Amount (USDT)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Text("USDT", color = TronGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp)) }
                    )

                    OutlinedTextField(
                        value = depositNoteInput,
                        onValueChange = { depositNoteInput = it },
                        label = { Text("Reference / Deposit Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Funds will be instantly credited to the user's available balance and verified in the database.",
                        color = TradeGreen,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = depositAmountInput.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && targetUserIdInput.isNotBlank()) {
                            viewModel.adminDepositToUser(targetUserIdInput.trim(), amt, depositNoteInput)
                            showDirectDepositModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TradeGreen)
                ) {
                    Text("Credit & Deposit Now", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectDepositModal = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = ObsidianCard
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Admin Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ApexTrade",
                            color = TronGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Admin Portal",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Admin User ID: Fadi Raees • Master Administrator",
                        color = Slate300,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { viewModel.navigateTo(AppNavDestination.DASHBOARD) },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("User View", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        // Direct Deposit Action Banner for Fadi Raees / MRG Company
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TradeGreenBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TradeGreen)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddCard, contentDescription = null, tint = TradeGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Deposit Funds into User ID",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Directly credit any trader's wallet balance in the database.",
                                color = Slate300,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Button(
                        onClick = { showDirectDepositModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("+ Deposit Fund", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Pending Action Banner if pending deposits or withdrawals exist
        if (pendingDeposits.isNotEmpty() || pendingWithdrawals.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TronGoldBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TronGold)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = TronGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pending Actions (${pendingDeposits.size} Deposits, ${pendingWithdrawals.size} Withdrawals)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Review incoming TRON TRC20 deposits and approve funds.",
                                    color = Slate300,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.navigateTo(AppNavDestination.ADMIN_DEPOSITS) },
                            colors = ButtonDefaults.buttonColors(containerColor = TronGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Review", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // High Level Metric Grid (2 Columns)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FintechMetricCard(
                        title = "Total Users",
                        value = "$totalUsers",
                        subtitle = "$activeUsers Active Accounts",
                        icon = Icons.Default.People,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_kpi_users"),
                        onClick = { viewModel.navigateTo(AppNavDestination.ADMIN_USERS) }
                    )
                    FintechMetricCard(
                        title = "Trading Volume",
                        value = "$${String.format(Locale.US, "%,.0f", totalVolume)}",
                        subtitle = "$openTradesCount Open Trades",
                        icon = Icons.Default.ShowChart,
                        valueColor = TradeGreen,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_kpi_volume"),
                        onClick = { viewModel.navigateTo(AppNavDestination.ADMIN_TRADES) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FintechMetricCard(
                        title = "Total Deposits",
                        value = "$${String.format(Locale.US, "%,.0f", totalDeposited)}",
                        subtitle = "${pendingDeposits.size} Pending ($${String.format(Locale.US, "%,.0f", pendingDepositSum)})",
                        icon = Icons.Default.ArrowDownward,
                        valueColor = TradeGreen,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_kpi_deposits"),
                        onClick = { viewModel.navigateTo(AppNavDestination.ADMIN_DEPOSITS) }
                    )
                    FintechMetricCard(
                        title = "Total Withdrawals",
                        value = "$${String.format(Locale.US, "%,.0f", totalWithdrawn)}",
                        subtitle = "${pendingWithdrawals.size} Pending ($${String.format(Locale.US, "%,.0f", pendingWithdrawalSum)})",
                        icon = Icons.Default.ArrowUpward,
                        valueColor = TronGold,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_kpi_withdrawals"),
                        onClick = { viewModel.navigateTo(AppNavDestination.ADMIN_WITHDRAWALS) }
                    )
                }
            }
        }

        // Live User Trades Surveillance on Dashboard
        val activeTrades = trades.filter { it.status == "OPEN" }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (activeTrades.isNotEmpty()) TradeGreen else Slate500, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeTrades.isNotEmpty()) "Live Active User Trades (${activeTrades.size})" else "User Trades Surveillance (${trades.size})",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = { viewModel.navigateTo(AppNavDestination.ADMIN_TRADES) }) {
                    Text("View All Trades (${trades.size})", color = TronGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (activeTrades.isNotEmpty()) {
            items(activeTrades) { t ->
                val elapsedSec = ((currentTime - t.createdAt) / 1000L).coerceAtLeast(0)
                val remainingSec = (t.durationSeconds - elapsedSec).coerceAtLeast(0)

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(TronGold.copy(alpha = 0.5f))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_live_trade_${t.id}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tracking ID: ${t.id}", color = TronGold, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                Text("Trader: ${t.username} • ${t.assetSymbol} (${t.direction})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (remainingSec > 10) TronGoldBg else TradeRedBg
                            ) {
                                Text(
                                    text = "⏱️ ${remainingSec}s left",
                                    color = if (remainingSec > 10) TronGold else TradeRed,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Margin: ${t.amount} USDT (${t.leverage}x)", color = Slate400, fontSize = 11.sp)
                            Text(
                                "Live PnL: ${if (t.pnl >= 0) "+$" else "-$"}${String.format(Locale.US, "%,.2f", abs(t.pnl))}",
                                color = if (t.pnl >= 0) TradeGreen else TradeRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.adminSetTradeWin(t.id, 85.0) },
                                colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("admin_dash_win_${t.id}")
                            ) {
                                Text("SET WIN (+85%)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.adminSetTradeLoss(t.id, "Admin Marked as LOSS") },
                                colors = ButtonDefaults.buttonColors(containerColor = TradeRed),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("admin_dash_loss_${t.id}")
                            ) {
                                Text("SET LOSS (-100%)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Show recent settled trades so admin always sees user trading activity
            val recentTrades = trades.filter { it.status != "OPEN" }.take(3)
            if (recentTrades.isEmpty()) {
                item {
                    Text("No user trades in database yet.", color = Slate500, fontSize = 12.sp)
                }
            } else {
                items(recentTrades) { t ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(t.id, color = TronGold, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StatusBadge(t.status)
                                }
                                Text("User: ${t.username} • ${t.assetSymbol} (${t.direction})", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                Text("Margin: $${t.amount} USDT", color = Slate400, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (t.pnl >= 0) "+$" else "-$"}${String.format(Locale.US, "%,.2f", abs(t.pnl))}",
                                    color = if (t.pnl >= 0) TradeGreen else TradeRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (t.pnl >= 0) "WIN (+85%)" else "LOSS (-100%)",
                                    color = if (t.pnl >= 0) TradeGreen else TradeRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Admin Navigation Directory
        item {
            Text(
                text = "Administrative Modules",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminNavCard("Live User Trades (WIN / LOSS)", "$openTradesCount Active Orders • Settle WIN / LOSS", Icons.AutoMirrored.Filled.TrendingUp, TradeGreen) {
                    viewModel.navigateTo(AppNavDestination.ADMIN_TRADES)
                }
                AdminNavCard("Review Deposits & Screenshots", "${pendingDeposits.size} Pending", Icons.Default.ReceiptLong, TronGold) {
                    viewModel.navigateTo(AppNavDestination.ADMIN_DEPOSITS)
                }
                AdminNavCard("Process Withdrawals", "${pendingWithdrawals.size} Pending", Icons.Default.Payments, TradeGreen) {
                    viewModel.navigateTo(AppNavDestination.ADMIN_WITHDRAWALS)
                }
                AdminNavCard("User Management & Balances", "$totalUsers Registered", Icons.Default.ManageAccounts, Color.White) {
                    viewModel.navigateTo(AppNavDestination.ADMIN_USERS)
                }
                AdminNavCard("Payment Methods & TRON Wallet", "TRC20: TB5vxHpnn5mq8TVvcCA4JTLdcLfsiBK3ZZ", Icons.Default.AccountBalanceWallet, TronGold) {
                    viewModel.navigateTo(AppNavDestination.ADMIN_PAYMENT_METHODS)
                }
                AdminNavCard("Immutable Audit Logs & DB Backups", "Full Financial Audit Trail", Icons.Default.Security, CyanAccent) {
                    viewModel.navigateTo(AppNavDestination.ADMIN_AUDIT_LOGS)
                }
            }
        }
    }
}

@Composable
private fun AdminNavCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(subtitle, color = Slate400, fontSize = 11.sp)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate500)
        }
    }
}
