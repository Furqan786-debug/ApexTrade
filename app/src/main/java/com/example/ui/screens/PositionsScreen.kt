package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.example.data.local.TradeEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun PositionsScreen(
    viewModel: TradingViewModel,
    openTrades: List<TradeEntity>,
    allTrades: List<TradeEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Open, 1: History
    val closedTrades = remember(allTrades) { allTrades.filter { it.status != "OPEN" } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Trading Positions",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ObsidianCard,
            contentColor = TradeGreen
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Open Positions (${openTrades.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Closed History (${closedTrades.size})", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        val currentList = if (selectedTab == 0) openTrades else closedTrades

        if (currentList.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = if (selectedTab == 0) "No active open positions." else "No closed trades yet.",
                    color = Slate500,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentList) { trade ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (trade.direction == "BUY_LONG") TradeGreenBg else TradeRedBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (trade.direction == "BUY_LONG") "BUY ${trade.leverage}X" else "SELL ${trade.leverage}X",
                                            color = if (trade.direction == "BUY_LONG") TradeGreen else TradeRed,
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
                                        fontSize = 15.sp
                                    )
                                }
                                StatusBadge(trade.status)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tracking ID: ${trade.id}",
                                    color = TronGold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(trade.createdAt)),
                                    color = Slate500,
                                    fontSize = 10.sp
                                )
                            }

                            if (trade.status == "OPEN") {
                                val elapsedSec = ((System.currentTimeMillis() - trade.createdAt) / 1000L).coerceAtLeast(0)
                                val remainingSec = (60 - elapsedSec).coerceAtLeast(0)

                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ObsidianSurfaceVariant,
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TronGold.copy(alpha = 0.5f))),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Auto-Settling in ${remainingSec}s",
                                            color = TronGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = "Live Surveillance Active",
                                            color = Slate400,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // PnL Highlight
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (trade.pnl >= 0) TradeGreenBg else TradeRedBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Unrealized PnL:", color = Slate400, fontSize = 11.sp)
                                    Text(
                                        text = "${if (trade.pnl >= 0) "+$" else "-$"}${String.format(Locale.US, "%,.2f", abs(trade.pnl))} (${if (trade.pnlPercentage >= 0) "+" else ""}${String.format(Locale.US, "%.2f", trade.pnlPercentage)}%)",
                                        color = if (trade.pnl >= 0) TradeGreen else TradeRed,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Entry Price", color = Slate500, fontSize = 10.sp)
                                    Text("$${trade.entryPrice}", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text("Current Price", color = Slate500, fontSize = 10.sp)
                                    Text("$${trade.currentPrice}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Margin", color = Slate500, fontSize = 10.sp)
                                    Text("$${trade.amount} USDT", color = Slate300, fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Size", color = Slate500, fontSize = 10.sp)
                                    Text("$${trade.positionSize} USDT", color = TronGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (trade.takeProfit != null || trade.stopLoss != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (trade.takeProfit != null) {
                                        Text("TP: $${trade.takeProfit}", color = TradeGreen, fontSize = 10.sp)
                                    }
                                    if (trade.stopLoss != null) {
                                        Text("SL: $${trade.stopLoss}", color = TradeRed, fontSize = 10.sp)
                                    }
                                }
                            }

                            if (trade.status == "OPEN") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.closeTrade(trade.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TradeRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("close_trade_${trade.id}")
                                ) {
                                    Text("Close Position & Settle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else if (trade.closingReason != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Closed: ${trade.closingReason}",
                                    color = Slate500,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
