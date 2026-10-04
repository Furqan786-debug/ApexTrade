package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TradeEntity
import com.example.ui.components.AdminPasswordLockDialog
import com.example.ui.components.DepositReviewDialog
import com.example.ui.components.TradeSettlementNotificationDialog
import com.example.ui.screens.*
import com.example.ui.screens.admin.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.TradingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApexTradeApp(viewModel: TradingViewModel) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentWallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val marketAssets by viewModel.marketAssets.collectAsStateWithLifecycle()
    val selectedAssetSymbol by viewModel.selectedAssetSymbol.collectAsStateWithLifecycle()
    val openTrades by viewModel.myOpenTrades.collectAsStateWithLifecycle()
    val myTrades by viewModel.myTrades.collectAsStateWithLifecycle()
    val myDeposits by viewModel.myDeposits.collectAsStateWithLifecycle()
    val myWithdrawals by viewModel.myWithdrawals.collectAsStateWithLifecycle()
    val myNotifications by viewModel.myNotifications.collectAsStateWithLifecycle()

    // Admin flows
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allTrades by viewModel.allTrades.collectAsStateWithLifecycle()
    val allDeposits by viewModel.allDeposits.collectAsStateWithLifecycle()
    val pendingDeposits by viewModel.pendingDeposits.collectAsStateWithLifecycle()
    val allWithdrawals by viewModel.allWithdrawals.collectAsStateWithLifecycle()
    val pendingWithdrawals by viewModel.pendingWithdrawals.collectAsStateWithLifecycle()
    val allPaymentMethods by viewModel.allPaymentMethods.collectAsStateWithLifecycle()
    val allAuditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()

    val showAdminPasswordPrompt by viewModel.showAdminPasswordDialog.collectAsStateWithLifecycle()
    val latestSettlementAlert by viewModel.latestSettlementAlert.collectAsStateWithLifecycle()
    val reviewingDepositId by viewModel.reviewingDepositId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Collect UI toast/snackbar messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Hardware Back Button handling
    BackHandler(enabled = currentScreen != AppNavDestination.AUTH && currentScreen != AppNavDestination.DASHBOARD) {
        if (!viewModel.handleBack()) {
            viewModel.navigateTo(AppNavDestination.DASHBOARD)
        }
    }

    // Dialog: Master Admin Password Lock (Prompt for Furqan@786)
    if (showAdminPasswordPrompt) {
        AdminPasswordLockDialog(
            onUnlock = { pass -> viewModel.unlockAdminWithPassword(pass) },
            onDismiss = { viewModel.closeAdminPasswordPrompt() }
        )
    }

    // Dialog: Real-Time Trade WIN/LOSS Notification on Screen with Exact Time
    if (latestSettlementAlert != null) {
        TradeSettlementNotificationDialog(
            alert = latestSettlementAlert!!,
            onDismiss = { viewModel.dismissSettlementAlert() }
        )
    }

    // Dialog: Review Deposit
    if (reviewingDepositId != null) {
        val dep = allDeposits.find { it.id == reviewingDepositId }
        if (dep != null) {
            DepositReviewDialog(
                deposit = dep,
                onApprove = { id, note -> viewModel.approveDeposit(id, note) },
                onReject = { id, reason -> viewModel.rejectDeposit(id, reason) },
                onDismiss = { viewModel.setReviewingDeposit(null) }
            )
        }
    }

    val isAdminSession = currentUser?.role != null && currentUser!!.role != "TRADER"
    val isInAdminMode = currentScreen.isAdminRoute

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentScreen != AppNavDestination.AUTH) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isInAdminMode) TronGold else TradeGreen,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "A",
                                        color = Color.Black,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ApexTrade",
                                        color = if (isInAdminMode) TronGold else Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp
                                    )
                                    if (isInAdminMode) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = TronGoldBg
                                        ) {
                                            Text(
                                                text = "ADMIN PORTAL",
                                                color = TronGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(if (isInAdminMode) TronGold else TradeGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isInAdminMode) "Admin Surveillance & Operations" else "Live • Latency 18ms",
                                        color = Slate400,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        // User Profile Pill (Clean profile shortcut - no account switcher popup)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ObsidianSurfaceVariant,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isAdminSession) TronGold else ObsidianBorder)
                            ),
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppNavDestination.PROFILE) }
                                .padding(end = 4.dp)
                                .testTag("appbar_user_profile_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAdminSession) Icons.Default.Shield else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isAdminSession) TronGold else TradeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentUser?.username ?: "Profile",
                                    color = if (isAdminSession) TronGold else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Mode Toggle (User vs Admin) - PASSWORD LOCKED with Furqan@786
                        IconButton(
                            onClick = {
                                if (isInAdminMode) {
                                    viewModel.navigateTo(AppNavDestination.DASHBOARD)
                                } else {
                                    // Require Furqan@786 admin password
                                    viewModel.openAdminPasswordPrompt()
                                }
                            },
                            modifier = Modifier.testTag("mode_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isInAdminMode) Icons.Default.Dashboard else Icons.Default.AdminPanelSettings,
                                contentDescription = "Toggle Portal",
                                tint = if (isInAdminMode) TronGold else Slate400
                            )
                        }

                        // Notification Bell
                        val unreadCount = myNotifications.count { !it.isRead }
                        IconButton(
                            onClick = { viewModel.navigateTo(AppNavDestination.NOTIFICATIONS) },
                            modifier = Modifier.testTag("notifications_bell_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = TradeRed) {
                                            Text("$unreadCount", color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Alerts",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ObsidianBg
                    )
                )
            }
        },
        bottomBar = {
            if (!isWideScreen && currentScreen != AppNavDestination.AUTH) {
                NavigationBar(
                    containerColor = ObsidianSurface,
                    contentColor = Color.White,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    if (isInAdminMode) {
                        // Admin Navigation Destinations
                        val adminTabs = listOf(
                            Triple(AppNavDestination.ADMIN_DASHBOARD, "Overview", Icons.Default.Dashboard),
                            Triple(AppNavDestination.ADMIN_TRADES, "Trades", Icons.AutoMirrored.Filled.TrendingUp),
                            Triple(AppNavDestination.ADMIN_DEPOSITS, "Deposits", Icons.Default.ReceiptLong),
                            Triple(AppNavDestination.ADMIN_WITHDRAWALS, "Withdrawals", Icons.Default.Payments),
                            Triple(AppNavDestination.ADMIN_USERS, "Users", Icons.Default.People)
                        )
                        adminTabs.forEach { (dest, label, icon) ->
                            val isSelected = currentScreen == dest
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(dest) },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TronGold,
                                    selectedTextColor = TronGold,
                                    indicatorColor = TronGoldBg,
                                    unselectedIconColor = Slate500,
                                    unselectedTextColor = Slate500
                                )
                            )
                        }
                    } else {
                        // User Trading Navigation Destinations
                        val userTabs = listOf(
                            Triple(AppNavDestination.DASHBOARD, "Home", Icons.Default.AccountBalanceWallet),
                            Triple(AppNavDestination.TRADE, "Trade", Icons.AutoMirrored.Filled.TrendingUp),
                            Triple(AppNavDestination.POSITIONS, "Positions", Icons.Default.PieChart),
                            Triple(AppNavDestination.DEPOSIT, "Deposit", Icons.Default.ArrowDownward),
                            Triple(AppNavDestination.PROFILE, "Profile", Icons.Default.Person)
                        )
                        userTabs.forEach { (dest, label, icon) ->
                            val isSelected = currentScreen == dest
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(dest) },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TradeGreen,
                                    selectedTextColor = TradeGreen,
                                    indicatorColor = TradeGreenBg,
                                    unselectedIconColor = Slate500,
                                    unselectedTextColor = Slate500
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = ObsidianBg
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Adaptive Navigation Rail for Wide Screens
            if (isWideScreen && currentScreen != AppNavDestination.AUTH) {
                NavigationRail(
                    containerColor = ObsidianSurface,
                    contentColor = Color.White
                ) {
                    val tabs = if (isInAdminMode) {
                        listOf(
                            Triple(AppNavDestination.ADMIN_DASHBOARD, "Overview", Icons.Default.Dashboard),
                            Triple(AppNavDestination.ADMIN_TRADES, "Trades", Icons.AutoMirrored.Filled.TrendingUp),
                            Triple(AppNavDestination.ADMIN_DEPOSITS, "Deposits", Icons.Default.ReceiptLong),
                            Triple(AppNavDestination.ADMIN_WITHDRAWALS, "Withdrawals", Icons.Default.Payments),
                            Triple(AppNavDestination.ADMIN_USERS, "Users", Icons.Default.People),
                            Triple(AppNavDestination.ADMIN_PAYMENT_METHODS, "Payments", Icons.Default.AccountBalance),
                            Triple(AppNavDestination.ADMIN_AUDIT_LOGS, "Audit", Icons.Default.Security)
                        )
                    } else {
                        listOf(
                            Triple(AppNavDestination.DASHBOARD, "Dashboard", Icons.Default.AccountBalanceWallet),
                            Triple(AppNavDestination.TRADE, "Trade", Icons.AutoMirrored.Filled.TrendingUp),
                            Triple(AppNavDestination.POSITIONS, "Positions", Icons.Default.PieChart),
                            Triple(AppNavDestination.DEPOSIT, "Deposit", Icons.Default.ArrowDownward),
                            Triple(AppNavDestination.WITHDRAW, "Withdraw", Icons.Default.ArrowUpward),
                            Triple(AppNavDestination.TRANSACTIONS, "History", Icons.Default.Receipt),
                            Triple(AppNavDestination.PROFILE, "Profile", Icons.Default.Person)
                        )
                    }

                    tabs.forEach { (dest, label, icon) ->
                        val isSelected = currentScreen == dest
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(dest) },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = if (isInAdminMode) TronGold else TradeGreen,
                                indicatorColor = if (isInAdminMode) TronGoldBg else TradeGreenBg,
                                unselectedIconColor = Slate500,
                                unselectedTextColor = Slate500
                            )
                        )
                    }
                }
            }

            // Screen Router with on-screen Live Trade Countdown Banner
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = if (isWideScreen) 1100.dp else 600.dp)
            ) {
                // Persistent On-Screen Trade Countdown Widget (Visible while user has open trades)
                if (currentScreen != AppNavDestination.AUTH && !isInAdminMode && openTrades.isNotEmpty()) {
                    ActiveTradesCountdownBanner(
                        openTrades = openTrades,
                        onBannerClick = { viewModel.navigateTo(AppNavDestination.TRADE) }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentScreen) {
                    AppNavDestination.AUTH -> {
                        AuthScreen(
                            viewModel = viewModel
                        )
                    }
                    AppNavDestination.DASHBOARD -> {
                        UserDashboardScreen(
                            viewModel = viewModel,
                            user = currentUser,
                            wallet = currentWallet,
                            assets = marketAssets,
                            openTrades = openTrades,
                            deposits = myDeposits
                        )
                    }
                    AppNavDestination.TRADE -> {
                        TradeScreen(
                            viewModel = viewModel,
                            selectedSymbol = selectedAssetSymbol,
                            assets = marketAssets,
                            wallet = currentWallet,
                            openTrades = openTrades
                        )
                    }
                    AppNavDestination.POSITIONS -> {
                        PositionsScreen(
                            viewModel = viewModel,
                            openTrades = openTrades,
                            allTrades = myTrades
                        )
                    }
                    AppNavDestination.DEPOSIT -> {
                        DepositScreen(
                            viewModel = viewModel,
                            paymentMethods = allPaymentMethods,
                            depositHistory = myDeposits
                        )
                    }
                    AppNavDestination.WITHDRAW -> {
                        WithdrawScreen(
                            viewModel = viewModel,
                            wallet = currentWallet,
                            withdrawals = myWithdrawals
                        )
                    }
                    AppNavDestination.TRANSACTIONS -> {
                        TransactionsScreen(
                            deposits = myDeposits,
                            withdrawals = myWithdrawals,
                            trades = myTrades
                        )
                    }
                    AppNavDestination.NOTIFICATIONS -> {
                        NotificationsScreen(
                            viewModel = viewModel,
                            notifications = myNotifications
                        )
                    }
                    AppNavDestination.PROFILE -> {
                        ProfileScreen(
                            tradingViewModel = viewModel
                        )
                    }
                    // Admin Routes
                    AppNavDestination.ADMIN_DASHBOARD -> {
                        AdminDashboardScreen(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            users = allUsers,
                            trades = allTrades,
                            deposits = allDeposits,
                            pendingDeposits = pendingDeposits,
                            withdrawals = allWithdrawals,
                            pendingWithdrawals = pendingWithdrawals
                        )
                    }
                    AppNavDestination.ADMIN_DEPOSITS -> {
                        AdminDepositsScreen(
                            viewModel = viewModel,
                            deposits = allDeposits
                        )
                    }
                    AppNavDestination.ADMIN_WITHDRAWALS -> {
                        AdminWithdrawalsScreen(
                            viewModel = viewModel,
                            withdrawals = allWithdrawals
                        )
                    }
                    AppNavDestination.ADMIN_USERS -> {
                        AdminUsersScreen(
                            viewModel = viewModel,
                            users = allUsers
                        )
                    }
                    AppNavDestination.ADMIN_TRADES -> {
                        AdminTradesScreen(
                            viewModel = viewModel,
                            trades = allTrades
                        )
                    }
                    AppNavDestination.ADMIN_PAYMENT_METHODS -> {
                        AdminPaymentMethodsScreen(
                            viewModel = viewModel,
                            paymentMethods = allPaymentMethods
                        )
                    }
                    AppNavDestination.ADMIN_AUDIT_LOGS -> {
                        AdminAuditLogsScreen(
                            viewModel = viewModel,
                            auditLogs = allAuditLogs
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ActiveTradesCountdownBanner(
    openTrades: List<TradeEntity>,
    onBannerClick: () -> Unit
) {
    val currentTime by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(1000)
            value = System.currentTimeMillis()
        }
    }

    val activeTrade = openTrades.firstOrNull() ?: return
    val elapsedSec = ((currentTime - activeTrade.createdAt) / 1000L).coerceAtLeast(0)
    val remainingSec = (activeTrade.durationSeconds - elapsedSec).coerceAtLeast(0)
    val isBuy = activeTrade.direction == "BUY_LONG"
    val progress = if (activeTrade.durationSeconds > 0) (remainingSec.toFloat() / activeTrade.durationSeconds.toFloat()).coerceIn(0f, 1f) else 0f

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSurfaceVariant,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (remainingSec > 10) TronGold else TradeRed
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onBannerClick() }
            .testTag("active_trade_countdown_banner")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (remainingSec > 10) TronGold else TradeRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE ORDER: ${activeTrade.assetSymbol}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isBuy) TradeGreenBg else TradeRedBg
                    ) {
                        Text(
                            text = if (isBuy) "BUY ${activeTrade.leverage}X" else "SELL ${activeTrade.leverage}X",
                            color = if (isBuy) TradeGreen else TradeRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Exact remaining seconds countdown
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (remainingSec > 10) TronGoldBg else TradeRedBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = if (remainingSec > 10) TronGold else TradeRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (remainingSec > 0) "⏱️ ${remainingSec}s left" else "Settling...",
                            color = if (remainingSec > 10) TronGold else TradeRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = if (remainingSec > 10) TronGold else TradeRed,
                trackColor = ObsidianBorder
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Margin: $${activeTrade.amount} USDT • Duration: ${activeTrade.durationSeconds}s",
                    color = Slate400,
                    fontSize = 10.sp
                )
                Text(
                    text = "Live PnL: ${if (activeTrade.pnl >= 0) "+$" else "-$"}${String.format(Locale.US, "%,.2f", abs(activeTrade.pnl))}",
                    color = if (activeTrade.pnl >= 0) TradeGreen else TradeRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
