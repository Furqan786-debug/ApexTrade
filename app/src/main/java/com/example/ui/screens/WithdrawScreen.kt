package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import com.example.data.local.WalletEntity
import com.example.data.local.WithdrawalEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WithdrawScreen(
    viewModel: TradingViewModel,
    wallet: WalletEntity?,
    withdrawals: List<WithdrawalEntity>,
    modifier: Modifier = Modifier
) {
    val availableBalance = wallet?.availableBalance ?: 0.0

    var selectedNetwork by remember { mutableStateOf("TRON (TRC20)") }
    var destinationAddress by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val networkFee = 1.0 // TRC20 fee
    val receiveAmount = (amount - networkFee).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Withdraw Funds",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Request secure payout to your external crypto wallet or bank account.",
                    color = Slate400,
                    fontSize = 12.sp
                )
            }
        }

        // Available Balance Notice
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Available for Withdrawal", color = Slate400, fontSize = 12.sp)
                        Text(
                            "$${String.format(Locale.US, "%,.2f", availableBalance)} USDT",
                            color = TradeGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TradeGreenBg
                    ) {
                        Text(
                            text = "Instant Review",
                            color = TradeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Payout Request Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Payout Network", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("TRON (TRC20)", "Ethereum (ERC20)", "Bank Wire").forEach { net ->
                            val isSelected = selectedNetwork == net
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) TronGold.copy(alpha = 0.2f) else ObsidianSurfaceVariant,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) TronGold else ObsidianBorder)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedNetwork = net }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = net.split(" ").first(),
                                        color = if (isSelected) TronGold else Slate400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Destination Address
                    OutlinedTextField(
                        value = destinationAddress,
                        onValueChange = { destinationAddress = it },
                        label = { Text("Destination Address / Account") },
                        placeholder = { Text("e.g. TYDzsYUE28p2a5vP31f2m4a1v5B8x9Q3rT") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_address_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Withdrawal Amount
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Withdrawal Amount (USDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input"),
                        singleLine = true,
                        trailingIcon = { Text("USDT", color = Slate400, modifier = Modifier.padding(end = 12.dp)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick percentage chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(25, 50, 75, 100).forEach { pct ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ObsidianSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val calc = (availableBalance * (pct / 100.0)).toInt()
                                        amountText = calc.toString()
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text("$pct%", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Breakdown
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ObsidianSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Network Fee:", color = Slate400, fontSize = 11.sp)
                                Text("$networkFee USDT", color = Color.White, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Receive Amount:", color = Slate400, fontSize = 11.sp)
                                Text("$${String.format(Locale.US, "%,.2f", receiveAmount)} USDT", color = TradeGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.submitWithdrawal(amount, selectedNetwork, destinationAddress)
                            amountText = ""
                            destinationAddress = ""
                        },
                        enabled = amount > 1.0 && amount <= availableBalance && destinationAddress.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_withdraw_btn")
                    ) {
                        Text(
                            text = "Submit Withdrawal Request",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Withdrawal History
        item {
            Text(
                text = "Withdrawal History (${withdrawals.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (withdrawals.isEmpty()) {
            item {
                Text(
                    text = "No withdrawal records found.",
                    color = Slate500,
                    fontSize = 12.sp
                )
            }
        } else {
            items(withdrawals) { wth ->
                Card(
                    shape = RoundedCornerShape(12.dp),
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
                            Text(
                                text = "-${wth.amount} USDT",
                                color = TradeRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            StatusBadge(wth.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Destination: ${wth.destinationAddress}", color = Slate400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        if (wth.txHash != null) {
                            Text("TX Hash: ${wth.txHash}", color = TronGold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(wth.createdAt)),
                            color = Slate500,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
