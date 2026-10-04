package com.example.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DepositEntity
import com.example.ui.components.DepositReviewDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDepositsScreen(
    viewModel: TradingViewModel,
    deposits: List<DepositEntity>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("PENDING") }
    var activeDepositForModal by remember { mutableStateOf<DepositEntity?>(null) }

    val filtered = remember(deposits, selectedFilter) {
        if (selectedFilter == "ALL") deposits
        else deposits.filter { it.status == selectedFilter }
    }

    if (activeDepositForModal != null) {
        DepositReviewDialog(
            deposit = activeDepositForModal!!,
            onApprove = { depId, note ->
                viewModel.approveDeposit(depId, note)
                activeDepositForModal = null
            },
            onReject = { depId, reason ->
                viewModel.rejectDeposit(depId, reason)
                activeDepositForModal = null
            },
            onDismiss = { activeDepositForModal = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Admin Deposit Approvals & Receipts",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Verify TRON TRC20 hashes, payment receipts & credit balances with audit logs",
            color = Slate400,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("PENDING", "APPROVED", "REJECTED", "ALL").forEach { status ->
                val isSelected = selectedFilter == status
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = status },
                    label = { Text(status) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (status == "PENDING") TronGold else TradeGreen,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filtered.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("No deposits matching filter '$selectedFilter'", color = Slate500)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { dep ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeDepositForModal = dep }
                            .testTag("admin_deposit_item_${dep.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${dep.id} • ${dep.username}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "User ID: ${dep.userId}",
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "+${dep.amount} USDT",
                                        color = TradeGreen,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    StatusBadge(dep.status)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Network: ${dep.network}",
                                color = TronGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "TX Hash: ${dep.txHash.take(28)}...",
                                color = Slate400,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Submitted: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(dep.createdAt))}",
                                color = Slate500,
                                fontSize = 10.sp
                            )

                            if (dep.userNote != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "User Note: ${dep.userNote}",
                                    color = Slate300,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, tint = TradeGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Screenshot Attached", color = TradeGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                if (dep.status == "PENDING") {
                                    Button(
                                        onClick = { activeDepositForModal = dep },
                                        colors = ButtonDefaults.buttonColors(containerColor = TronGold),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Review & Settle", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                } else {
                                    Text("Reviewed", color = Slate500, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
