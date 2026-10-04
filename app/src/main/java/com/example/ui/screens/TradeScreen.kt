package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TradeEntity
import com.example.data.local.WalletEntity
import com.example.data.model.MarketAsset
import com.example.data.model.TradeDirection
import com.example.ui.components.TradingChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs

@Composable
fun TradeScreen(
    viewModel: TradingViewModel,
    selectedSymbol: String,
    assets: List<MarketAsset>,
    wallet: WalletEntity?,
    openTrades: List<TradeEntity>,
    modifier: Modifier = Modifier
) {
    val currentAsset = assets.find { it.symbol == selectedSymbol } ?: assets.firstOrNull()
    val availableBalance = wallet?.availableBalance ?: 0.0

    var tradeDirection by remember { mutableStateOf(TradeDirection.UP) }
    var leverage by remember { mutableIntStateOf(10) }
    var marginAmountText by remember { mutableStateOf("100") }
    var selectedDuration by remember { mutableIntStateOf(60) }
    var selectedProfitPercentage by remember { mutableDoubleStateOf(25.0) }
    var slText by remember { mutableStateOf("") }
    var tpText by remember { mutableStateOf("") }

    // Live ticking timer for on-screen countdowns
    val currentTime by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(1000)
            value = System.currentTimeMillis()
        }
    }

    val marginAmount = marginAmountText.toDoubleOrNull() ?: 0.0
    val positionSize = marginAmount * leverage
    val currentPrice = currentAsset?.price ?: 1.0

    // Profit tier unlocks based on trade amount
    // $100 - $500: 10% to 25% profit
    // $500 - $1,000: unlocks up to 60% profit
    // $1,000+: unlocks 70% to 100% full profit
    val isSilverTierUnlocked = marginAmount >= 500.0
    val isGoldTierUnlocked = marginAmount >= 1000.0

    // Ensure selected percentage does not exceed current tier
    LaunchedEffect(marginAmount) {
        if (!isGoldTierUnlocked && selectedProfitPercentage > 60.0) {
            selectedProfitPercentage = if (isSilverTierUnlocked) 50.0 else 25.0
        } else if (!isSilverTierUnlocked && selectedProfitPercentage > 25.0) {
            selectedProfitPercentage = 25.0
        }
    }

    // Estimate liquidation price
    val estLiqPrice = remember(currentPrice, leverage, tradeDirection) {
        val maxLossRatio = 0.90 / leverage
        val isUp = tradeDirection == TradeDirection.UP || tradeDirection == TradeDirection.BUY_LONG
        if (isUp) {
            currentPrice * (1.0 - maxLossRatio)
        } else {
            currentPrice * (1.0 + maxLossRatio)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // Asset Selector Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(assets) { asset ->
                    val isSelected = asset.symbol == selectedSymbol
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) TradeGreen.copy(alpha = 0.2f) else ObsidianCard,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) TradeGreen else ObsidianBorder)
                        ),
                        modifier = Modifier
                            .clickable { viewModel.selectAsset(asset.symbol) }
                            .testTag("select_pair_${asset.symbol}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = asset.symbol,
                                color = if (isSelected) Color.White else Slate400,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (asset.change24h >= 0) "+" else ""}${asset.change24h}%",
                                color = if (asset.change24h >= 0) TradeGreen else TradeRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Real-Time Chart
        if (currentAsset != null) {
            item {
                TradingChart(
                    symbol = currentAsset.symbol,
                    currentPrice = currentAsset.price,
                    change24h = currentAsset.change24h
                )
            }
        }

        // Trading Execution Order Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Direction Switcher: UP vs DOWN (Binary Options Style)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianSurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = { tradeDirection = TradeDirection.UP },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tradeDirection == TradeDirection.UP || tradeDirection == TradeDirection.BUY_LONG) TradeGreen else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("direction_up")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (tradeDirection == TradeDirection.UP || tradeDirection == TradeDirection.BUY_LONG) Color.Black else TradeGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "UP ⬆",
                                    color = if (tradeDirection == TradeDirection.UP || tradeDirection == TradeDirection.BUY_LONG) Color.Black else Slate300,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Button(
                            onClick = { tradeDirection = TradeDirection.DOWN },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tradeDirection == TradeDirection.DOWN || tradeDirection == TradeDirection.SELL_SHORT) TradeRed else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("direction_down")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (tradeDirection == TradeDirection.DOWN || tradeDirection == TradeDirection.SELL_SHORT) Color.White else TradeRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "DOWN ⬇",
                                    color = if (tradeDirection == TradeDirection.DOWN || tradeDirection == TradeDirection.SELL_SHORT) Color.White else Slate300,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Leverage Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Leverage Multiplier", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("${leverage}x", color = TronGold, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 5, 10, 20, 50).forEach { lev ->
                            val isSelected = leverage == lev
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) TronGold.copy(alpha = 0.2f) else ObsidianSurfaceVariant,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) TronGold else ObsidianBorder)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { leverage = lev }
                                    .testTag("lev_${lev}x")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${lev}x",
                                        color = if (isSelected) TronGold else Slate400,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Margin Amount Input (Min $100 Start Rule)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Trade Amount (Min $100 USDT)", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Avail: $${String.format(Locale.US, "%,.2f", availableBalance)}",
                            color = TradeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = marginAmountText,
                        onValueChange = { marginAmountText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("margin_input_field"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        trailingIcon = { Text("USDT", color = TronGold, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(end = 12.dp)) }
                    )

                    if (marginAmount > 0 && marginAmount < 100.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠ Trade starts from minimum $100 USDT",
                            color = TradeRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Margin Presets ($100, $250, $500, $1000, $2500)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(100, 250, 500, 1000, 2500).forEach { presetVal ->
                            val isSelected = marginAmount.toInt() == presetVal
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) TronGold.copy(alpha = 0.25f) else ObsidianSurfaceVariant,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) TronGold else ObsidianBorder)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { marginAmountText = presetVal.toString() }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$$presetVal",
                                        color = if (isSelected) TronGold else Slate300,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Profit Percentage Tier Selector (10% to 100% based on user rules)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = TradeGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Target Profit Payout (10% - 100%)", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("+${selectedProfitPercentage.toInt()}% Profit", color = TradeGreen, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tier Level Info Card
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ObsidianSurfaceVariant,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isGoldTierUnlocked) TronGold else if (isSilverTierUnlocked) TradeGreen else ObsidianBorder
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when {
                                    isGoldTierUnlocked -> "👑 VIP Tier ($1000+): 100% Max Profit Unlocked"
                                    isSilverTierUnlocked -> "🥈 Silver Tier ($500+): Up to 60% Profit Unlocked"
                                    else -> "🟢 Standard Tier ($100-$500): 10%–25% Profit Payout"
                                },
                                color = if (isGoldTierUnlocked) TronGold else if (isSilverTierUnlocked) TradeGreen else Slate300,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!isGoldTierUnlocked) {
                                Text(
                                    text = if (!isSilverTierUnlocked) "Deposit/Trade $500 for more" else "Trade $1000 for 100%",
                                    color = TronGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Profit Percentage Selection Chips (10% to 100%)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val profitRates = listOf(
                            10.0 to 100.0,
                            15.0 to 100.0,
                            20.0 to 100.0,
                            25.0 to 100.0,
                            35.0 to 500.0,
                            50.0 to 500.0,
                            60.0 to 500.0,
                            75.0 to 1000.0,
                            85.0 to 1000.0,
                            100.0 to 1000.0
                        )
                        items(profitRates) { (rate, requiredAmount) ->
                            val isUnlocked = marginAmount >= requiredAmount
                            val isSelected = selectedProfitPercentage == rate

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    isSelected -> TradeGreen.copy(alpha = 0.25f)
                                    isUnlocked -> ObsidianSurfaceVariant
                                    else -> ObsidianSurface.copy(alpha = 0.5f)
                                },
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        when {
                                            isSelected -> TradeGreen
                                            isUnlocked -> ObsidianBorder
                                            else -> ObsidianBorder.copy(alpha = 0.4f)
                                        }
                                    )
                                ),
                                modifier = Modifier.clickable {
                                    if (isUnlocked) {
                                        selectedProfitPercentage = rate
                                    } else {
                                        // Auto-adjust margin amount to unlock tier!
                                        marginAmountText = requiredAmount.toInt().toString()
                                        selectedProfitPercentage = rate
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!isUnlocked) {
                                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Slate500, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "${rate.toInt()}%",
                                        color = when {
                                            isSelected -> TradeGreen
                                            isUnlocked -> Color.White
                                            else -> Slate500
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Trade Duration Selector (Contract Expiry Time set by user)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = TronGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trade Expiry Duration", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Text("${selectedDuration}s", color = TronGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            30 to "30s",
                            60 to "60s (1m)",
                            120 to "120s (2m)",
                            300 to "300s (5m)"
                        ).forEach { (sec, label) ->
                            val isSelected = selectedDuration == sec
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) TronGold.copy(alpha = 0.2f) else ObsidianSurfaceVariant,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) TronGold else ObsidianBorder)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedDuration = sec }
                                    .testTag("duration_${sec}s")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) TronGold else Slate400,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Position & Payout Calculation Preview Card
                    val estProfit = marginAmount * (selectedProfitPercentage / 100.0)
                    val totalReturn = marginAmount + estProfit

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ObsidianSurfaceVariant,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Direction & Leverage:", color = Slate400, fontSize = 11.sp)
                                val isUp = tradeDirection == TradeDirection.UP || tradeDirection == TradeDirection.BUY_LONG
                                Text(
                                    text = if (isUp) "UP ⬆ (${leverage}x)" else "DOWN ⬇ (${leverage}x)",
                                    color = if (isUp) TradeGreen else TradeRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Potential Profit (+${selectedProfitPercentage.toInt()}%):", color = Slate400, fontSize = 11.sp)
                                Text("+$${String.format(Locale.US, "%.2f", estProfit)} USDT", color = TradeGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Win Return:", color = Slate400, fontSize = 11.sp)
                                Text("$${String.format(Locale.US, "%.2f", totalReturn)} USDT", color = TronGold, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Contract Expiry Duration:", color = Slate400, fontSize = 11.sp)
                                Text("${selectedDuration} Seconds", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (availableBalance <= 0.0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TronGoldBg,
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TronGold)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = TronGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Zero Balance Account", color = TronGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Your balance is 0.00 USDT. You cannot open positions until you make your first deposit.",
                                    color = Slate300,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.navigateTo(com.example.ui.viewmodel.AppNavDestination.DEPOSIT) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TronGold),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Deposit TRON (TRC20)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Execution Button (UP or DOWN with Profit Payout)
                    val isUp = tradeDirection == TradeDirection.UP || tradeDirection == TradeDirection.BUY_LONG
                    val canExecute = marginAmount >= 100.0 && marginAmount <= availableBalance
                    Button(
                        onClick = {
                            viewModel.executeTrade(
                                assetSymbol = selectedSymbol,
                                direction = tradeDirection,
                                amount = marginAmount,
                                leverage = leverage,
                                stopLoss = slText.toDoubleOrNull(),
                                takeProfit = tpText.toDoubleOrNull(),
                                durationSeconds = selectedDuration,
                                profitPercentage = selectedProfitPercentage
                            )
                        },
                        enabled = canExecute,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUp) TradeGreen else TradeRed,
                            disabledContainerColor = ObsidianBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_order_btn")
                    ) {
                        Text(
                            text = if (isUp) {
                                "Trade UP ⬆ ($${marginAmount.toInt()} USDT • +${selectedProfitPercentage.toInt()}% Profit)"
                            } else {
                                "Trade DOWN ⬇ ($${marginAmount.toInt()} USDT • +${selectedProfitPercentage.toInt()}% Profit)"
                            },
                            color = if (isUp) Color.Black else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Active Positions for Current Asset with Real-Time Countdown
        val assetPositions = openTrades.filter { it.assetSymbol == selectedSymbol }
        item {
            Text(
                text = "Active Positions on $selectedSymbol (${assetPositions.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        if (assetPositions.isEmpty()) {
            item {
                Text(
                    text = "No active positions on $selectedSymbol.",
                    color = Slate500,
                    fontSize = 12.sp
                )
            }
        } else {
            items(assetPositions) { pos ->
                val elapsedSec = ((currentTime - pos.createdAt) / 1000L).coerceAtLeast(0)
                val remainingSec = (pos.durationSeconds - elapsedSec).coerceAtLeast(0)
                val progress = if (pos.durationSeconds > 0) (remainingSec.toFloat() / pos.durationSeconds.toFloat()).coerceIn(0f, 1f) else 0f

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (remainingSec > 10) TronGold.copy(alpha = 0.5f) else TradeRed.copy(alpha = 0.8f)
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_pos_${pos.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val isUpPos = pos.direction == "UP" || pos.direction == "BUY_LONG"
                                Surface(
                                    color = if (isUpPos) TradeGreenBg else TradeRedBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isUpPos) "UP ⬆ ${pos.leverage}X" else "DOWN ⬇ ${pos.leverage}X",
                                        color = if (isUpPos) TradeGreen else TradeRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = pos.id,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TronGold,
                                    fontSize = 12.sp
                                )
                            }

                            // Live Countdown Badge on phone screen
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

                        // Progress bar showing time remaining
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
                                Text("Margin: $${pos.amount} USDT", color = Slate400, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Entry: $${pos.entryPrice} -> Now: $${pos.currentPrice}", color = Color.White, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Duration: ${pos.durationSeconds}s • Target Profit: +${pos.profitPercentage.toInt()}%", color = TronGold, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (pos.pnl >= 0) "+$" else "-$"}${String.format(Locale.US, "%,.2f", abs(pos.pnl))}",
                                    color = if (pos.pnl >= 0) TradeGreen else TradeRed,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { viewModel.closeTrade(pos.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBorder),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Close Now", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
