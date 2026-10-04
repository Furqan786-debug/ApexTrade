package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DepositEntity
import com.example.data.local.TradeEntity
import com.example.data.local.WithdrawalEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TransactionItem(
    val id: String,
    val type: String, // DEPOSIT, WITHDRAWAL, TRADE
    val title: String,
    val amountFormatted: String,
    val isPositive: Boolean,
    val status: String,
    val timestamp: Long,
    val details: String
)

@Composable
fun TransactionsScreen(
    deposits: List<DepositEntity>,
    withdrawals: List<WithdrawalEntity>,
    trades: List<TradeEntity>,
    modifier: Modifier = Modifier
) {
    var filterType by remember { mutableStateOf("ALL") }

    val allTransactions = remember(deposits, withdrawals, trades) {
        val list = mutableListOf<TransactionItem>()

        deposits.forEach { d ->
            list.add(
                TransactionItem(
                    id = d.id,
                    type = "DEPOSIT",
                    title = "Deposit (${d.network})",
                    amountFormatted = "+$${d.amount} USDT",
                    isPositive = true,
                    status = d.status,
                    timestamp = d.createdAt,
                    details = "TX: ${d.txHash.take(18)}..."
                )
            )
        }

        withdrawals.forEach { w ->
            list.add(
                TransactionItem(
                    id = w.id,
                    type = "WITHDRAWAL",
                    title = "Withdrawal (${w.network})",
                    amountFormatted = "-$${w.amount} USDT",
                    isPositive = false,
                    status = w.status,
                    timestamp = w.createdAt,
                    details = "To: ${w.destinationAddress.take(18)}..."
                )
            )
        }

        trades.filter { it.status != "OPEN" }.forEach { t ->
            list.add(
                TransactionItem(
                    id = t.id,
                    type = "TRADE",
                    title = "Trade Settle: ${t.assetSymbol}",
                    amountFormatted = "${if (t.pnl >= 0) "+$" else "-$"}${kotlin.math.abs(t.pnl)} USDT",
                    isPositive = t.pnl >= 0,
                    status = t.status,
                    timestamp = t.closedAt ?: t.createdAt,
                    details = "${t.direction} • PnL ${t.pnlPercentage}%"
                )
            )
        }

        list.sortedByDescending { it.timestamp }
    }

    val filteredList = remember(allTransactions, filterType) {
        if (filterType == "ALL") allTransactions
        else allTransactions.filter { it.type == filterType }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Financial Activity & Transactions",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "DEPOSIT", "WITHDRAWAL", "TRADE").forEach { cat ->
                val isSelected = filterType == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { filterType = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TradeGreen,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("No transactions found.", color = Slate500)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { tx ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
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
                                Text(tx.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(tx.details, color = Slate400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(tx.timestamp)),
                                    color = Slate500,
                                    fontSize = 10.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = tx.amountFormatted,
                                    color = if (tx.isPositive) TradeGreen else TradeRed,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StatusBadge(tx.status)
                            }
                        }
                    }
                }
            }
        }
    }
}
