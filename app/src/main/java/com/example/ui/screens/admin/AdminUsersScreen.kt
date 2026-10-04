package com.example.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.data.model.AccountStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingViewModel

@Composable
fun AdminUsersScreen(
    viewModel: TradingViewModel,
    users: List<UserEntity>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForEdit by remember { mutableStateOf<UserEntity?>(null) }
    var showAdjustBalanceModal by remember { mutableStateOf(false) }
    var adjustAmountText by remember { mutableStateOf("") }
    var adjustReasonText by remember { mutableStateOf("") }

    var showDepositModal by remember { mutableStateOf(false) }
    var directDepositAmountText by remember { mutableStateOf("500") }
    var directDepositNoteText by remember { mutableStateOf("MRG Company Direct Deposit") }

    // Modal for Direct Fund Deposit to User ID
    if (showDepositModal && selectedUserForEdit != null) {
        val target = selectedUserForEdit!!
        AlertDialog(
            onDismissRequest = { showDepositModal = false },
            title = {
                Text(
                    text = "Deposit Funds to User ID",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Credit funds directly to User: ${target.username} (${target.id})",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = directDepositAmountText,
                        onValueChange = { directDepositAmountText = it },
                        label = { Text("Deposit Amount (USDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = { Text("USDT", color = TronGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp)) }
                    )
                    OutlinedTextField(
                        value = directDepositNoteText,
                        onValueChange = { directDepositNoteText = it },
                        label = { Text("Deposit Reference / Note") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "Funds will be credited immediately to the user's available balance in the database.",
                        color = TradeGreen,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = directDepositAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.adminDepositToUser(target.id, amt, directDepositNoteText)
                            showDepositModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TradeGreen)
                ) {
                    Text("Deposit Now", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositModal = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = ObsidianCard
        )
    }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else {
            val q = searchQuery.lowercase().trim()
            users.filter {
                it.id.lowercase().contains(q) ||
                it.username.lowercase().contains(q) ||
                it.email.lowercase().contains(q) ||
                it.phone.lowercase().contains(q)
            }
        }
    }

    // Modal for Transparent Financial Adjustment
    if (showAdjustBalanceModal && selectedUserForEdit != null) {
        val target = selectedUserForEdit!!
        AlertDialog(
            onDismissRequest = { showAdjustBalanceModal = false },
            title = {
                Text(
                    text = "Financial Correction / Balance Adjustment",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Target User: ${target.username} (${target.id})",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = adjustAmountText,
                        onValueChange = { adjustAmountText = it },
                        label = { Text("Adjustment Amount (+ / - USDT)") },
                        placeholder = { Text("e.g. +500 or -250") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = adjustReasonText,
                        onValueChange = { adjustReasonText = it },
                        label = { Text("Mandatory Transparent Audit Reason") },
                        placeholder = { Text("e.g. Approved promotion grant / dispute reversal") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Zero-Tolerance Policy: All adjustments are published to the immutable audit log and notified to the user.",
                        color = TronGold,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val delta = adjustAmountText.toDoubleOrNull() ?: 0.0
                        if (delta != 0.0 && adjustReasonText.length >= 5) {
                            viewModel.adjustUserBalance(target.id, delta, adjustReasonText)
                            showAdjustBalanceModal = false
                            adjustAmountText = ""
                            adjustReasonText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TradeGreen)
                ) {
                    Text("Apply & Log", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustBalanceModal = false }) {
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
            text = "User Directory & Management",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Search users, audit profiles, adjust balances with mandatory audit logs",
            color = Slate400,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by User ID, Username, Email, Phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_user_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredUsers) { user ->
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
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StatusBadge(user.role)
                                }
                                Text("ID: ${user.id} • ${user.email}", color = Slate400, fontSize = 11.sp)
                                Text("Phone: ${user.phone}", color = Slate500, fontSize = 11.sp)
                            }
                            StatusBadge(user.accountStatus)
                        }

                        if (user.internalNotes != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Internal Note: ${user.internalNotes}", color = Slate300, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Admin Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    selectedUserForEdit = user
                                    directDepositAmountText = "500"
                                    showDepositModal = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Text("+ Deposit", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val next = if (user.accountStatus == "ACTIVE") AccountStatus.SUSPENDED else AccountStatus.ACTIVE
                                    viewModel.updateUserStatus(user.id, next, "Admin toggle from dashboard")
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Text(
                                    text = if (user.accountStatus == "ACTIVE") "Suspend" else "Activate",
                                    fontSize = 11.sp,
                                    color = if (user.accountStatus == "ACTIVE") TradeRed else TradeGreen
                                )
                            }

                            Button(
                                onClick = {
                                    selectedUserForEdit = user
                                    showAdjustBalanceModal = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TronGold),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Text("Adjust", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
