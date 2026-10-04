package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.TradingViewModel
import com.example.ui.viewmodel.UserProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    tradingViewModel: TradingViewModel,
    userProfileViewModel: UserProfileViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by userProfileViewModel.uiState.collectAsStateWithLifecycle()
    val currentTrader by tradingViewModel.currentUser.collectAsStateWithLifecycle()
    val currentWallet by tradingViewModel.currentWallet.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    // Synchronized active user values ensuring new accounts immediately display all signup info
    val effectiveUsername = currentTrader?.username?.ifBlank { uiState.username } ?: uiState.username
    val effectiveEmail = currentTrader?.email?.ifBlank { uiState.email } ?: uiState.email
    val effectivePhone = currentTrader?.phone?.ifBlank { uiState.phone } ?: uiState.phone
    val effectiveUid = currentTrader?.id ?: uiState.uniqueId
    val effectiveStatus = currentTrader?.accountStatus ?: uiState.accountStatus
    val effectiveRole = currentTrader?.role ?: uiState.role
    val effectiveBalance = currentWallet?.totalBalance ?: uiState.totalBalance
    val effectiveAvail = currentWallet?.availableBalance ?: uiState.availableBalance
    val effectiveLocked = currentWallet?.lockedMargin ?: uiState.lockedMargin

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var copiedId by remember { mutableStateOf(false) }

    // Dialog for editing profile
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit Profile Information",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "UID: $effectiveUid",
                        color = Slate400,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Full Name / Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TradeGreen) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_username_input")
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email (Gmail)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TradeGreen) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_email_input")
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TradeGreen) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_phone_input")
                    )
                    Text(
                        text = "Changes will be permanently updated in the central database record.",
                        color = Slate500,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userProfileViewModel.updateProfile(editUsername, editEmail, editPhone)
                        tradingViewModel.updateUserProfile(editUsername, editEmail, editPhone)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                    modifier = Modifier.testTag("save_profile_btn")
                ) {
                    Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = ObsidianCard
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "User Profile & Identity",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Live database record: $effectiveUid",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = {
                        editUsername = effectiveUsername
                        editEmail = effectiveEmail
                        editPhone = effectivePhone
                        showEditProfileDialog = true
                    },
                    modifier = Modifier.testTag("edit_profile_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = TronGold
                    )
                }
            }
        }

        // Primary User Details Identity Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_identity_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(58.dp)
                                .background(TradeGreen.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Text(
                                text = (effectiveUsername.firstOrNull() ?: 'U').uppercase(),
                                color = TradeGreen,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = effectiveUsername.ifBlank { "User" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(effectiveRole)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "UID: $effectiveUid",
                                    color = TronGold,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(effectiveUid))
                                        copiedId = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (copiedId) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy UID",
                                        tint = if (copiedId) TradeGreen else Slate400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        StatusBadge(effectiveStatus)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = ObsidianBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileFieldRow("Email (Gmail)", effectiveEmail.ifBlank { "Not provided" }, Icons.Default.Email)
                    ProfileFieldRow("Phone Number", effectivePhone.ifBlank { "Not provided" }, Icons.Default.Phone)
                    ProfileFieldRow("Account Status", effectiveStatus, Icons.Default.VerifiedUser)
                    ProfileFieldRow("KYC Identity Verification", if (uiState.isVerified) "Verified (Level 2)" else "Pending", Icons.Default.Badge)
                    ProfileFieldRow(
                        "Member Since",
                        if (uiState.registeredDate > 0) SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(uiState.registeredDate)) else "Today",
                        Icons.Default.CalendarToday
                    )
                }
            }
        }

        // Zero Balance & Deposit Alert Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isZeroBalance) TronGoldBg else ObsidianCard
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (uiState.isZeroBalance) TronGold else ObsidianBorder)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_balance_status_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACCOUNT WALLET BALANCE",
                            color = if (uiState.isZeroBalance) TronGold else Slate400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        if (uiState.isZeroBalance) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TronGold.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "DEPOSIT REQUIRED",
                                    color = TronGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", uiState.totalBalance)}",
                            color = if (uiState.isZeroBalance) Color.White else TradeGreen,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " USDT",
                            color = Slate400,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bilingual / Clear notice that balance stays zero until deposit
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ObsidianSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.isZeroBalance) Icons.Default.Info else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (uiState.isZeroBalance) TronGold else TradeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (uiState.isZeroBalance) {
                                    "New account balance remains 0.00 USDT until you make a deposit. Please deposit TRON (TRC20) to fund your wallet and start trading."
                                } else {
                                    "Wallet is active with ${String.format(Locale.US, "%,.2f", uiState.totalBalance)} USDT total equity and ${String.format(Locale.US, "%,.2f", uiState.totalDeposited)} USDT lifetime deposits."
                                },
                                color = Slate300,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Deposit CTA Button
                    Button(
                        onClick = { tradingViewModel.navigateTo(AppNavDestination.DEPOSIT) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (uiState.isZeroBalance) TronGold else TradeGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("profile_deposit_cta_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isZeroBalance) "Make First Deposit (TRON TRC20)" else "Deposit More Funds",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Security & Two-Factor Authentication
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Security Settings", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Two-Factor Authentication (2FA)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Enhances login and withdrawal protection", color = Slate500, fontSize = 11.sp)
                        }
                        Switch(
                            checked = uiState.twoFactorEnabled,
                            onCheckedChange = { userProfileViewModel.toggleTwoFactor(uiState.twoFactorEnabled) }
                        )
                    }
                }
            }
        }

        // Session Logout Action
        item {
            Button(
                onClick = { tradingViewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = TradeRed.copy(alpha = 0.15f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TradeRed)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_profile_btn")
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = TradeRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out of Account", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileFieldRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Slate500, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = Slate400, fontSize = 12.sp)
        }
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
