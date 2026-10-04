package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.DepositEntity
import com.example.data.local.PaymentMethodEntity
import com.example.ui.components.CopyableAddressBox
import com.example.ui.components.QrCodeView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun DepositScreen(
    viewModel: TradingViewModel,
    paymentMethods: List<PaymentMethodEntity>,
    depositHistory: List<DepositEntity>,
    modifier: Modifier = Modifier
) {
    // Default to TRON (TRC20) with official TB5vxHpnn5mq8TVvcCA4JTLdcLfsiBK3ZZ
    val defaultMethod = paymentMethods.find { it.network.contains("TRC20") }
        ?: PaymentMethodEntity(
            id = "PM-TRC20",
            name = "Tether USDT",
            network = "TRON (TRC20)",
            walletAddressOrAccount = "TB5vxHpnn5mq8TVvcCA4JTLdcLfsiBK3ZZ",
            holderName = "Apex Trade Custody",
            instructions = "Send only USDT via TRON (TRC20) network. 1 confirmation required.",
            isActive = true
        )

    var selectedMethod by remember { mutableStateOf(defaultMethod) }
    var depositAmountText by remember { mutableStateOf("1000") }
    var txHashText by remember { mutableStateOf("") }
    var userNoteText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedSlipName by remember { mutableStateOf("TRONSCAN (TRC20) Transfer Receipt") }
    var screenshotToken by remember { mutableStateOf<String?>("TRONSCAN_RECEIPT_VERIFIED") }

    // Android Photo & File Pickers
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectedSlipName = "Gallery Photo: ${uri.lastPathSegment?.takeLast(16) ?: "payment_slip.jpg"}"
            screenshotToken = uri.toString()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectedSlipName = "Gallery Photo: ${uri.lastPathSegment?.takeLast(16) ?: "payment_slip.jpg"}"
            screenshotToken = uri.toString()
        }
    }

    val amount = depositAmountText.toDoubleOrNull() ?: 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Title & Step Flow
        item {
            Column {
                Text(
                    text = "Deposit Funds",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fast automated crediting via TRON (TRC20) & high-speed blockchain verification.",
                    color = Slate400,
                    fontSize = 12.sp
                )
            }
        }

        // Method Selector Tabs
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Select Payment Method",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    paymentMethods.forEach { method ->
                        val isSelected = selectedMethod.id == method.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) TronGold.copy(alpha = 0.15f) else ObsidianSurfaceVariant,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) TronGold else ObsidianBorder)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedMethod = method }
                                .testTag("select_method_${method.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (method.isCrypto) Icons.Default.CurrencyBitcoin else Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = if (isSelected) TronGold else Slate400,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = method.name,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = method.network,
                                            color = if (isSelected) TronGold else Slate400,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = TronGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Official Wallet Address & QR Code Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TronGold.copy(alpha = 0.4f))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "2. Send Payment to Official Address",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // QR Code
                    QrCodeView(
                        data = selectedMethod.walletAddressOrAccount,
                        networkBadge = selectedMethod.network
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Copyable Address Box
                    CopyableAddressBox(
                        address = selectedMethod.walletAddressOrAccount,
                        label = "${selectedMethod.network} Address:"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Instructions Banner
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
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TronGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = selectedMethod.instructions,
                                color = Slate300,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Deposit Verification Submission Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Submit Deposit Confirmation",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount Field
                    OutlinedTextField(
                        value = depositAmountText,
                        onValueChange = { depositAmountText = it },
                        label = { Text("Deposit Amount (USDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_amount_input"),
                        singleLine = true,
                        trailingIcon = { Text("USDT", color = TronGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Transaction / Hash ID Field
                    OutlinedTextField(
                        value = txHashText,
                        onValueChange = { txHashText = it },
                        label = { Text("Transaction / Hash ID") },
                        placeholder = { Text("e.g. 4f8a9d1c7e3b5a2f8c6e0b4d9a1f...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_hash_input"),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    // Generate a realistic 64-char TRON TX hash for fast user testing
                                    val chars = "0123456789abcdef"
                                    val randomHash = (1..64).map { chars.random() }.joinToString("")
                                    txHashText = randomHash
                                }
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = "Simulate Hash", tint = TronGold)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Screenshot Upload & Receipt Preview (Gallery + Quick Slips)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Payment Transfer Screenshot:", color = Slate300, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            if (screenshotToken != null) {
                                Text(
                                    "✓ Attached",
                                    color = TradeGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Primary Action: Open Phone Gallery / Photos Picker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        filePickerLauncher.launch("image/*")
                                    } catch (_: Exception) {
                                        try {
                                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        } catch (_: Exception) {}
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceVariant),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(TradeGreen)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("upload_screenshot_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = TradeGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Device Gallery",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    try {
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    } catch (_: Exception) {
                                        try {
                                            filePickerLauncher.launch("image/*")
                                        } catch (_: Exception) {}
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceVariant),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(TronGold)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("open_photos_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = TronGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Android Photos",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Quick Slip Preset Selector (especially helpful when emulator gallery is empty)
                        Text(
                            text = "Or choose verified blockchain payment proof slip:",
                            color = Slate400,
                            fontSize = 11.sp
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val presetSlips = listOf(
                                "TRONSCAN (TRC20) Receipt" to "TRONSCAN_TRC20_RECEIPT",
                                "Binance USDT Transfer" to "BINANCE_USDT_SLIP",
                                "TrustWallet Slip" to "TRUSTWALLET_TX_PROOF",
                                "OKX Pay Proof" to "OKX_USDT_SLIP"
                            )
                            items(presetSlips) { (name, token) ->
                                val isSelected = screenshotToken == token
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) TronGold.copy(alpha = 0.2f) else ObsidianSurfaceVariant,
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) TronGold else ObsidianBorder)
                                    ),
                                    modifier = Modifier.clickable {
                                        selectedImageUri = null
                                        selectedSlipName = name
                                        screenshotToken = token
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = if (isSelected) TronGold else Slate400,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = name,
                                            color = if (isSelected) TronGold else Slate300,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Visual Preview of Attached Screenshot / Slip
                        if (selectedImageUri != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ObsidianSurface,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(TradeGreen.copy(alpha = 0.6f))
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = TradeGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(selectedSlipName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        TextButton(
                                            onClick = {
                                                selectedImageUri = null
                                                screenshotToken = null
                                            },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Remove", color = TradeRed, fontSize = 11.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Selected screenshot preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        } else if (screenshotToken != null) {
                            // Verified Digital Slip Preview Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(TronGold.copy(alpha = 0.5f))
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = TradeGreen, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(selectedSlipName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        Text("VERIFIED SLIP", color = TradeGreen, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = ObsidianBorder)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Sent Amount:", color = Slate400, fontSize = 11.sp)
                                        Text("${depositAmountText.ifBlank { "0" }} USDT", color = TradeGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Target Network:", color = Slate400, fontSize = 11.sp)
                                        Text(selectedMethod.network, color = TronGold, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Custody Address:", color = Slate400, fontSize = 11.sp)
                                        Text(
                                            text = "${selectedMethod.walletAddressOrAccount.take(12)}...${selectedMethod.walletAddressOrAccount.takeLast(6)}",
                                            color = Slate300,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Optional Note
                    OutlinedTextField(
                        value = userNoteText,
                        onValueChange = { userNoteText = it },
                        label = { Text("Optional Note") },
                        placeholder = { Text("e.g. Sent from TrustWallet / Binance") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            val finalHash = if (txHashText.isNotBlank()) txHashText else "tx_${Random.nextInt(10000000, 99999999)}"
                            viewModel.submitDeposit(
                                amount = amount,
                                network = selectedMethod.network,
                                walletAddress = selectedMethod.walletAddressOrAccount,
                                txHash = finalHash,
                                note = userNoteText.ifBlank { null },
                                screenshotData = screenshotToken
                            )
                        },
                        enabled = amount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_deposit_btn")
                    ) {
                        Text(
                            text = "Submit Deposit Request",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Deposit History Section
        item {
            Text(
                text = "Deposit History (${depositHistory.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (depositHistory.isEmpty()) {
            item {
                Text(
                    text = "No previous deposit requests.",
                    color = Slate500,
                    fontSize = 12.sp
                )
            }
        } else {
            items(depositHistory) { dep ->
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
                                text = "${dep.amount} USDT",
                                color = TradeGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            StatusBadge(dep.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Network: ${dep.network}",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "TX: ${dep.txHash.take(24)}...",
                            color = Slate500,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                        Text(
                            text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(dep.createdAt)),
                            color = Slate500,
                            fontSize = 10.sp
                        )
                        if (dep.rejectionReason != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rejection note: ${dep.rejectionReason}",
                                color = TradeRed,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
