package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DepositEntity
import com.example.data.local.TradeEntity
import com.example.data.local.UserEntity
import com.example.data.local.WalletEntity
import com.example.data.model.MarketAsset
import com.example.ui.components.FintechMetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.TradingViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs

@Composable
fun UserDashboardScreen(
    viewModel: TradingViewModel,
    user: UserEntity?,
    wallet: WalletEntity?,
    assets: List<MarketAsset>,
    openTrades: List<TradeEntity>,
    deposits: List<DepositEntity>,
    modifier: Modifier = Modifier
) {
    val totalBalance = wallet?.totalBalance ?: 0.0
    val availableBalance = wallet?.availableBalance ?: 0.0
    val lockedMargin = wallet?.lockedMargin ?: 0.0
    val totalPnl = wallet?.totalProfitLoss ?: 0.0

    val currentTime by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(1000)
            value = System.currentTimeMillis()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Welcome Header & Session Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Apex Trading Terminal",
                        color = Slate400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Hello, ${user?.username ?: "Trader"}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianSurfaceVariant,
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(TradeGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = user?.id ?: "Trader",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Main Portfolio Balance Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("portfolio_balance_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    TradeGreenBg,
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL ESTIMATED BALANCE",
                                color = Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            StatusBadge(user?.accountStatus ?: "ACTIVE")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", totalBalance)}",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = " USDT",
                                color = Slate400,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sub balances: Available, Margin, Total PnL
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Available Balance", color = Slate500, fontSize = 11.sp)
                                Text(
                                    "$${String.format(Locale.US, "%,.2f", availableBalance)}",
                                    color = TradeGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("In Open Margin", color = Slate500, fontSize = 11.sp)
                                Text(
                                    "$${String.format(Locale.US, "%,.2f", lockedMargin)}",
                                    color = TronGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Realized PnL", color = Slate500, fontSize = 11.sp)
                                Text(
                                    "${if (totalPnl >= 0) "+$" else "-$"}${String.format(Locale.US, "%,.2f", abs(totalPnl))}",
                                    color = if (totalPnl >= 0) TradeGreen else TradeRed,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Action Buttons: Deposit (TRON TRC20), Withdraw, Trade
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.navigateTo(AppNavDestination.DEPOSIT) },
                                colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("quick_deposit_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Deposit", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.navigateTo(AppNavDestination.WITHDRAW) },
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = ObsidianSurfaceVariant),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("quick_withdraw_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Withdraw", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.navigateTo(AppNavDestination.TRADE) },
                                colors = ButtonDefaults.buttonColors(containerColor = TronGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("quick_trade_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trade", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Live Market Tickers Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Market Watch",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live Ticker • 2s",
                    color = TradeGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Horizontal Carousel of Market Assets
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(assets) { asset ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                        modifier = Modifier
                            .width(150.dp)
                            .clickable {
                                viewModel.selectAsset(asset.symbol)
                                viewModel.navigateTo(AppNavDestination.TRADE)
                            }
                            .testTag("asset_card_${asset.symbol}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = asset.symbol,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (asset.change24h >= 0) TradeGreenBg else TradeRedBg
                                ) {
                                    Text(
                                        text = "${if (asset.change24h >= 0) "+" else ""}${asset.change24h}%",
                                        color = if (asset.change24h >= 0) TradeGreen else TradeRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", asset.price)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (asset.change24h >= 0) TradeGreen else TradeRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Vol: $${String.format(Locale.US, "%.1fM", asset.volume24h / 1_000_000)}",
                                color = Slate500,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Active Open Positions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Open Positions (${openTrades.size})",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    color = TronGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.navigateTo(AppNavDestination.POSITIONS) }
                )
            }
        }

        if (openTrades.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianCard,
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Open Positions", color = Slate400, fontWeight = FontWeight.Bold)
                        Text("Open a Buy or Sell position from the Trade terminal.", color = Slate500, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(openTrades) { trade ->
                val elapsedSec = ((currentTime - trade.createdAt) / 1000L).coerceAtLeast(0)
                val remainingSec = (trade.durationSeconds - elapsedSec).coerceAtLeast(0)
                val progress = if (trade.durationSeconds > 0) (remainingSec.toFloat() / trade.durationSeconds.toFloat()).coerceIn(0f, 1f) else 0f

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (remainingSec > 10) TronGold.copy(alpha = 0.5f) else TradeRed.copy(alpha = 0.8f)
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val isUp = trade.direction == "UP" || trade.direction == "BUY_LONG"
                                Surface(
                                    color = if (isUp) TradeGreenBg else TradeRedBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isUp) "UP ⬆ ${trade.leverage}X" else "DOWN ⬇ ${trade.leverage}X",
                                        color = if (isUp) TradeGreen else TradeRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = trade.assetSymbol,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }

                            // Live countdown badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (remainingSec > 10) TronGoldBg else TradeRedBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = if (remainingSec > 10) TronGold else TradeRed,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (remainingSec > 0) "${remainingSec}s left" else "Settling...",
                                        color = if (remainingSec > 10) TronGold else TradeRed,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = if (remainingSec > 10) TronGold else TradeRed,
                            trackColor = ObsidianSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Margin: $${trade.amount} USDT", color = Slate400, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Entry: $${trade.entryPrice} -> Now: $${trade.currentPrice}", color = Color.White, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Time: ${trade.durationSeconds}s duration", color = Slate500, fontSize = 10.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (trade.pnl >= 0) "+$" else "-$"}${String.format(Locale.US, "%,.2f", abs(trade.pnl))}",
                                    color = if (trade.pnl >= 0) TradeGreen else TradeRed,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { viewModel.closeTrade(trade.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Close", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Deposits Tracker Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = TronGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TRC20 Fast Deposit Status",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "Deposit",
                            color = TradeGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.navigateTo(AppNavDestination.DEPOSIT) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val latestDeposit = deposits.firstOrNull()
                    if (latestDeposit != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ObsidianSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${latestDeposit.amount} USDT • ${latestDeposit.network}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "TX: ${latestDeposit.txHash.take(16)}...",
                                        color = Slate400,
                                        fontSize = 10.sp
                                    )
                                }
                                StatusBadge(latestDeposit.status)
                            }
                        }
                    } else {
                        Text(
                            text = "Deposit TRON (TRC20) USDT for instant zero-fee funding.",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
