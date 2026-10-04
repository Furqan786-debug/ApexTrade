package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.local.WithdrawalEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun AdminWithdrawalsScreen(
    viewModel: TradingViewModel,
    withdrawals: List<WithdrawalEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("PENDING") }
    var processingWithdrawal by remember { mutableStateOf<WithdrawalEntity?>(null) }
    var isRejectMode by remember { mutableStateOf(false) }
    var txHashInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    val filtered = remember(withdrawals, selectedTab) {
        if (selectedTab == "ALL") withdrawals
        else withdrawals.filter { it.status == selectedTab }
    }

    // Modal Dialog for Complete / Reject
    if (processingWithdrawal != null) {
        val w = processingWithdrawal!!
        AlertDialog(
            onDismissRequest = { processingWithdrawal = null },
            title = {
                Text(
                    text = if (isRejectMode) "Reject & Refund Withdrawal" else "Complete & Broadcast Payout",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("User: ${w.username} (${w.userId})", color = Slate400, fontSize = 12.sp)
                    Text("Amount: ${w.amount} USDT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Destination: ${w.destinationAddress}", color = Slate400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                    if (!isRejectMode) {
                        OutlinedTextField(
                            value = txHashInput,
                            onValueChange = { txHashInput = it },
                            label = { Text("Blockchain TX Hash") },
                            placeholder = { Text("Enter TRON or bank confirmation hash") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            label = { Text("Admin Note") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            label = { Text("Mandatory Rejection Reason") },
                            placeholder = { Text("e.g. Invalid address, compliance review") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Text(
                            text = "Rejecting will immediately refund ${w.amount} USDT to user available balance and write an audit record.",
                            color = TradeRed,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                if (isRejectMode) {
                    Button(
                        onClick = {
                            viewModel.rejectWithdrawal(w.id, noteInput.ifBlank { "Administrative policy check" })
                            processingWithdrawal = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TradeRed)
                    ) {
                        Text("Confirm Rejection", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            val hash = txHashInput.ifBlank { "0x_${Random.nextInt(10000000, 99999999)}" }
                            viewModel.completeWithdrawal(w.id, hash, noteInput.ifBlank { "Sent on-chain" })
                            processingWithdrawal = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TradeGreen)
                    ) {
                        Text("Complete Payout", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { processingWithdrawal = null }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = ObsidianCard
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "MRG Company • Withdrawal Payout Processing",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Review user payout requests with User Name, User ID & Tracking ID",
            color = Slate400,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("PENDING", "COMPLETED", "REJECTED", "ALL").forEach { tab ->
                val isSelected = selectedTab == tab
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    label = { Text(tab) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (tab == "PENDING") TronGold else TradeGreen,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filtered.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("No withdrawals in '$selectedTab'", color = Slate500)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { w ->
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
                                Column {
                                    Text(
                                        text = "Tracking ID: ${w.id}",
                                        color = TronGold,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("User Name: ${w.username}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("User ID: ${w.userId}", color = Slate400, fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("-${w.amount} USDT", color = TradeRed, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                    StatusBadge(w.status)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Network: ${w.network}", color = TronGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Destination Address: ${w.destinationAddress}", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            Text(
                                "Requested Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(w.createdAt))}",
                                color = Slate500,
                                fontSize = 10.sp
                            )

                            if (w.status == "PENDING") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            isRejectMode = true
                                            noteInput = "Address verification failed"
                                            processingWithdrawal = w
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TradeRed),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reject & Refund")
                                    }

                                    Button(
                                        onClick = {
                                            isRejectMode = false
                                            txHashInput = "tx_${Random.nextInt(10000000, 99999999)}"
                                            noteInput = "Sent via TRON Custody"
                                            processingWithdrawal = w
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Complete", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
