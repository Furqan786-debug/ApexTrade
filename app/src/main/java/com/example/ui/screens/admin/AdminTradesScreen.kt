package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun AdminTradesScreen(
    viewModel: TradingViewModel,
    trades: List<TradeEntity>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val currentTime by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(1000)
            value = System.currentTimeMillis()
        }
    }

    val filtered = remember(trades, selectedFilter) {
        when (selectedFilter) {
            "OPEN" -> trades.filter { it.status == "OPEN" }
            "CLOSED" -> trades.filter { it.status != "OPEN" }
            else -> trades
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ApexTrade Admin • Trade Surveillance & Decision",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live user orders with User Name, Tracking ID & WIN / LOSS control",
                    color = Slate400,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rule banner explaining default loss if admin doesn't mark win
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = ObsidianSurfaceVariant,
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TronGold.copy(alpha = 0.5f))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TronGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Admin Rule: You can mark any trade as WIN or LOSS. If admin does not click WIN, by default the trade will settle as LOSS when user duration ends.",
                    color = Slate300,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val openCount = trades.count { it.status == "OPEN" }
        val closedCount = trades.count { it.status != "OPEN" }
        val allCount = trades.size

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("ALL", "All Trades ($allCount)", TradeGreen),
                Triple("OPEN", "Open Orders ($openCount)", TronGold),
                Triple("CLOSED", "Settled ($closedCount)", Slate400)
            ).forEach { (filterKey, label, chipColor) ->
                val isSelected = selectedFilter == filterKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filterKey },
                    label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chipColor,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("No trades in '$selectedFilter'", color = Slate500)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { t ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (t.status == "OPEN") TronGold.copy(alpha = 0.4f) else ObsidianBorder
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_trade_${t.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header: Tracking ID & User Name
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Tracking ID: ${t.id}",
                                            color = TronGold,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        StatusBadge(t.status)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "User Name: ${t.username} (User ID: ${t.userId})",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Text(
                                    text = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(t.createdAt)),
                                    color = Slate500,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Trade specs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Instrument", color = Slate500, fontSize = 10.sp)
                                    Text(
                                        "${t.assetSymbol} (${t.leverage}x)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Column {
                                    Text("Direction", color = Slate500, fontSize = 10.sp)
                                    val isUp = t.direction == "UP" || t.direction.contains("BUY")
                                    Text(
                                        if (isUp) "UP ⬆" else "DOWN ⬇",
                                        color = if (isUp) TradeGreen else TradeRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Column {
                                    Text("Margin", color = Slate500, fontSize = 10.sp)
                                    Text("${t.amount} USDT", color = Slate300, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Live PnL", color = Slate500, fontSize = 10.sp)
                                    Text(
                                        "${if (t.pnl >= 0) "+$" else "-$"}${String.format(Locale.US, "%,.2f", abs(t.pnl))}",
                                        color = if (t.pnl >= 0) TradeGreen else TradeRed,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Entry: $${t.entryPrice} • Live: $${t.currentPrice}",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                                Text(
                                    "Target: +${t.profitPercentage.toInt()}% Profit",
                                    color = TronGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            // Admin WIN / LOSS Controls for OPEN trades
                            if (t.status == "OPEN") {
                                val elapsedSec = ((currentTime - t.createdAt) / 1000L).coerceAtLeast(0)
                                val remainingSec = (t.durationSeconds - elapsedSec).coerceAtLeast(0)

                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (remainingSec > 10) TronGoldBg else TradeRedBg,
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(if (remainingSec > 10) TronGold else TradeRed)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Alarm,
                                                contentDescription = null,
                                                tint = if (remainingSec > 10) TronGold else TradeRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Auto-Loss in ${remainingSec}s unless SET WIN is clicked (User set: ${t.durationSeconds}s)",
                                                color = if (remainingSec > 10) TronGold else TradeRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Text(
                                            text = "Default: LOSS",
                                            color = Slate400,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.adminSetTradeWin(t.id, 85.0) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("admin_win_${t.id}")
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("SET WIN (+85%)", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.adminSetTradeLoss(t.id, "Admin Marked as LOSS") },
                                        colors = ButtonDefaults.buttonColors(containerColor = TradeRed),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("admin_loss_${t.id}")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("SET LOSS (-100%)", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Outcome: ${t.closingReason ?: (t.adminDecision ?: "Settled")}",
                                    color = if (t.pnl >= 0) TradeGreen else TradeRed,
                                    fontWeight = FontWeight.Bold,
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
