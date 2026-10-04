package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.DepositEntity
import com.example.data.local.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.TradeSettlementAlert
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FintechMetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    valueColor: Color = Color.White,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Slate400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Slate500,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status.uppercase()) {
        "APPROVED", "COMPLETED", "ACTIVE" -> TradeGreenBg to TradeGreen
        "PENDING", "PROCESSING", "PENDING_VERIFICATION" -> TronGoldBg to TronGold
        "REJECTED", "SUSPENDED", "LIQUIDATED" -> TradeRedBg to TradeRed
        "OPEN" -> CyanAccent.copy(alpha = 0.15f) to CyanAccent
        "CLOSED" -> Slate700.copy(alpha = 0.3f) to Slate400
        else -> Slate700 to Slate200
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = status,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun CopyableAddressBox(
    address: String,
    label: String = "Deposit Address",
    modifier: Modifier = Modifier,
    onCopied: () -> Unit = {}
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Slate400,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ObsidianSurfaceVariant, RoundedCornerShape(10.dp))
                .border(1.dp, ObsidianBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = address,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(address))
                    copied = true
                    onCopied()
                },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("copy_address_btn")
            ) {
                Icon(
                    imageVector = if (copied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                    contentDescription = "Copy address",
                    tint = if (copied) TradeGreen else TronGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AccountSwitcherDialog(
    allUsers: List<UserEntity>,
    currentUserId: String,
    onSelectUser: (String) -> Unit,
    onRegisterUser: (username: String, email: String, phone: String) -> Unit,
    onGoogleLogin: ((email: String, name: String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var isAdminLoginMode by remember { mutableStateOf(false) }

    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }

    var adminUserIdInput by remember { mutableStateOf("Fadi Raees") }
    var adminPasswordInput by remember { mutableStateOf("Furqan@786") }
    var adminAuthError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when {
                    isAdminLoginMode -> "MRG Company • Admin Portal Login"
                    isRegisterMode -> "Create Account (0.00 Balance)"
                    else -> "Switch Account / Login"
                },
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            when {
                isAdminLoginMode -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TronGoldBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "MRG Company Portal: User ID: 'Fadi Raees', Password: 'Furqan@786'",
                                color = TronGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        OutlinedTextField(
                            value = adminUserIdInput,
                            onValueChange = { adminUserIdInput = it },
                            label = { Text("Admin User ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = adminPasswordInput,
                            onValueChange = { adminPasswordInput = it },
                            label = { Text("Admin Password") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (adminAuthError != null) {
                            Text(adminAuthError!!, color = TradeRed, fontSize = 11.sp)
                        }
                    }
                }
                isRegisterMode -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "New accounts start with 0.00 USDT balance. Make a deposit via TRON (TRC20) to fund your account and start trading.",
                            color = TronGold,
                            fontSize = 11.sp
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp)
                    ) {
                        // Quick MRG Company Admin Shortcut
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TronGoldBg,
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TronGold)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val fadi = allUsers.find { it.id == "Fadi Raees" || it.username == "Fadi Raees" }
                                    if (fadi != null) onSelectUser(fadi.id) else isAdminLoginMode = true
                                }
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("MRG Company Master Admin", color = TronGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("User ID: Fadi Raees • Pass: Furqan@786", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                                Text("Login", color = TronGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Google Sign-In Simulation Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onGoogleLogin?.invoke("google.trader@gmail.com", "Google Trader")
                                }
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Continue with Google (0.00 Balance)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = "Select an existing account or test session:",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        allUsers.forEach { user ->
                            val isSelected = user.id == currentUserId
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) TradeGreen.copy(alpha = 0.15f) else ObsidianSurfaceVariant,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) TradeGreen else ObsidianBorder)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        onSelectUser(user.id)
                                    }
                                    .testTag("select_user_${user.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = user.username,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            StatusBadge(user.role)
                                        }
                                        Text(
                                            text = "${user.id} • ${user.email}",
                                            color = Slate400,
                                            fontSize = 10.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active",
                                            tint = TradeGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when {
                isAdminLoginMode -> {
                    Button(
                        onClick = {
                            if (adminUserIdInput.trim() == "Fadi Raees" && adminPasswordInput == "Furqan@786") {
                                val adminUser = allUsers.find { it.id == "Fadi Raees" || it.username == "Fadi Raees" }
                                if (adminUser != null) {
                                    onSelectUser(adminUser.id)
                                } else {
                                    onSelectUser("Fadi Raees")
                                }
                            } else {
                                adminAuthError = "Invalid credentials. Use Fadi Raees & Furqan@786"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TronGold)
                    ) {
                        Text("Verify & Enter", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                isRegisterMode -> {
                    Button(
                        onClick = {
                            if (regUsername.isNotBlank() && regEmail.isNotBlank()) {
                                onRegisterUser(regUsername, regEmail, regPhone)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TradeGreen)
                    ) {
                        Text("Register (0.00 Balance)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { isAdminLoginMode = true }) {
                            Text("MRG Admin", color = TronGold, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { isRegisterMode = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TradeGreen)
                        ) {
                            Text("+ New User", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (isRegisterMode || isAdminLoginMode) {
                    isRegisterMode = false
                    isAdminLoginMode = false
                } else onDismiss()
            }) {
                Text("Cancel", color = Slate400)
            }
        },
        containerColor = ObsidianCard
    )
}

@Composable
fun DepositReviewDialog(
    deposit: DepositEntity,
    onApprove: (depositId: String, note: String) -> Unit,
    onReject: (depositId: String, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var adminNote by remember { mutableStateOf("Verified on TRONSCAN blockchain explorer") }
    var rejectionReason by remember { mutableStateOf("Transaction hash not found on TRON explorer") }
    var showRejectInput by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Deposit Review: ${deposit.id}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                StatusBadge(deposit.status)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // User & Amount Banner
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
                            Text("User:", color = Slate400, fontSize = 12.sp)
                            Text("${deposit.username} (${deposit.userId})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount:", color = Slate400, fontSize = 12.sp)
                            Text("${deposit.amount} USDT", color = TradeGreen, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Network:", color = Slate400, fontSize = 12.sp)
                            Text(deposit.network, color = TronGold, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }

                // Blockchain Tx Hash
                Column {
                    Text("Transaction / Hash ID:", color = Slate400, fontSize = 11.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ObsidianSurface,
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = deposit.txHash,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Payment Screenshot Receipt Preview
                Column {
                    Text("Payment Screenshot Preview:", color = Slate400, fontSize = 11.sp)
                    val isUri = deposit.screenshotData?.startsWith("content://") == true || deposit.screenshotData?.startsWith("file://") == true
                    if (isUri) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black,
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TradeGreen.copy(alpha = 0.6f))),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = deposit.screenshotData,
                                    contentDescription = "User Payment Screenshot",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TradeGreen.copy(alpha = 0.4f))),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = when (deposit.screenshotData) {
                                            "BINANCE_USDT_SLIP" -> "BINANCE PAY RECEIPT"
                                            "TRUSTWALLET_TX_PROOF" -> "TRUSTWALLET SLIP"
                                            else -> "TRONSCAN (TRC20) RECEIPT"
                                        },
                                        color = TronGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text("VERIFIED SLIP", color = TradeGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Column {
                                    Text("Transfer: ${deposit.amount} USDT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("To: ${deposit.walletAddress}", color = Slate400, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Hash: ${deposit.txHash.take(24)}...", color = Slate500, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                                Text(
                                    "Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(deposit.createdAt))}",
                                    color = Slate400,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                if (deposit.userNote != null) {
                    Text("User Note: ${deposit.userNote}", color = Slate300, fontSize = 11.sp)
                }

                if (deposit.status == "PENDING") {
                    if (showRejectInput) {
                        OutlinedTextField(
                            value = rejectionReason,
                            onValueChange = { rejectionReason = it },
                            label = { Text("Mandatory Rejection Reason") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            value = adminNote,
                            onValueChange = { adminNote = it },
                            label = { Text("Approval Note (Audit Log)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (deposit.status == "PENDING") {
                if (showRejectInput) {
                    Button(
                        onClick = { onReject(deposit.id, rejectionReason) },
                        colors = ButtonDefaults.buttonColors(containerColor = TradeRed)
                    ) {
                        Text("Confirm Rejection", color = Color.White)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showRejectInput = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TradeRed)
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = { onApprove(deposit.id, adminNote) },
                            colors = ButtonDefaults.buttonColors(containerColor = TradeGreen)
                        ) {
                            Text("Approve & Credit", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate400)
            }
        },
        containerColor = ObsidianCard
    )
}

@Composable
fun AdminPasswordLockDialog(
    onUnlock: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TronGoldBg,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Admin Lock",
                            tint = TronGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "MRG Company",
                        color = TronGold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Master Admin Portal Lock",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianSurfaceVariant,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(TronGold.copy(alpha = 0.5f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "SECURITY RESTRICTION",
                            color = TronGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "This administrative module is strictly restricted to Fadi Raees (Master Administrator). Please enter the admin security password to proceed.",
                            color = Slate300,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    label = { Text("Admin Password") },
                    leadingIcon = {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = TronGold)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = Slate400
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_lock_password_input")
                )

                if (errorMessage != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = TradeRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = errorMessage!!,
                            color = TradeRed,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val success = onUnlock(passwordInput)
                    if (!success) {
                        errorMessage = "Access Denied: Incorrect Password! You must enter the valid admin password (Furqan@786)."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TronGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("admin_unlock_btn")
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Unlock Portal", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate400)
            }
        },
        containerColor = ObsidianCard
    )
}

@Composable
fun TradeSettlementNotificationDialog(
    alert: TradeSettlementAlert,
    onDismiss: () -> Unit
) {
    val isWin = alert.outcome.equals("WIN", ignoreCase = true)
    val accentColor = if (isWin) TradeGreen else TradeRed

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isWin) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isWin) "TRADE WON! 🎉" else "TRADE CLOSED: LOSS ❌",
                        color = accentColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "MRG Company Real-Time Engine",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Big PnL Display
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(accentColor)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isWin) "NET PROFIT" else "TOTAL LOSS",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = alert.pnlText,
                            color = accentColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Table of details
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SettlementInfoRow("Asset Symbol", alert.assetSymbol, Icons.Default.CurrencyBitcoin)
                        SettlementInfoRow("Tracking ID", alert.tradeId, Icons.Default.Tag)
                        SettlementInfoRow("Account User", alert.username, Icons.Default.Person)
                        SettlementInfoRow("Settlement Time", alert.formattedTime, Icons.Default.Schedule, highlight = true)
                        SettlementInfoRow("Settled Reason", alert.reason, Icons.Default.Info)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dismiss_settlement_alert_btn")
            ) {
                Text(
                    text = if (isWin) "Acknowledge Win (+$${alert.pnlText.substringAfter("+$").substringBefore(" ")})" else "Acknowledge Result",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        containerColor = ObsidianCard
    )
}

@Composable
private fun SettlementInfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = Slate400, fontSize = 11.sp)
        }
        Text(
            text = value,
            color = if (highlight) TronGold else Color.White,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 11.sp,
            fontFamily = if (highlight) FontFamily.Monospace else FontFamily.Default
        )
    }
}
