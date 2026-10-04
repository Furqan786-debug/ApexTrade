package com.example.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.local.PaymentMethodEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingViewModel
import kotlin.random.Random

@Composable
fun AdminPaymentMethodsScreen(
    viewModel: TradingViewModel,
    paymentMethods: List<PaymentMethodEntity>,
    modifier: Modifier = Modifier
) {
    var editingMethod by remember { mutableStateOf<PaymentMethodEntity?>(null) }
    var isNewMethod by remember { mutableStateOf(false) }

    var nameInput by remember { mutableStateOf("") }
    var networkInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var holderInput by remember { mutableStateOf("") }
    var instructionsInput by remember { mutableStateOf("") }
    var isActiveInput by remember { mutableStateOf(true) }

    fun openEdit(pm: PaymentMethodEntity?, isNew: Boolean) {
        editingMethod = pm
        isNewMethod = isNew
        if (pm != null && !isNew) {
            nameInput = pm.name
            networkInput = pm.network
            addressInput = pm.walletAddressOrAccount
            holderInput = pm.holderName
            instructionsInput = pm.instructions
            isActiveInput = pm.isActive
        } else {
            nameInput = "Tether USDT"
            networkInput = "TRON (TRC20)"
            addressInput = "TB5vxHpnn5mq8TVvcCA4JTLdcLfsiBK3ZZ"
            holderInput = "Apex Trade Custody"
            instructionsInput = "Send TRC20 USDT only. 1 network confirmation required."
            isActiveInput = true
        }
    }

    if (editingMethod != null || isNewMethod) {
        AlertDialog(
            onDismissRequest = {
                editingMethod = null
                isNewMethod = false
            },
            title = {
                Text(
                    text = if (isNewMethod) "Add Payment Method / Wallet" else "Edit Payment Method",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Method Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = networkInput,
                        onValueChange = { networkInput = it },
                        label = { Text("Network / Transfer Type") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Wallet Address / Bank Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = holderInput,
                        onValueChange = { holderInput = it },
                        label = { Text("Account Holder / Custody Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = instructionsInput,
                        onValueChange = { instructionsInput = it },
                        label = { Text("Instructions shown to Users") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active for User Deposits", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = isActiveInput,
                            onCheckedChange = { isActiveInput = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = editingMethod?.id ?: "PM-${Random.nextInt(1000, 9999)}"
                        val updated = PaymentMethodEntity(
                            id = id,
                            name = nameInput,
                            network = networkInput,
                            walletAddressOrAccount = addressInput,
                            holderName = holderInput,
                            instructions = instructionsInput,
                            isActive = isActiveInput
                        )
                        viewModel.savePaymentMethod(updated)
                        editingMethod = null
                        isNewMethod = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TradeGreen)
                ) {
                    Text("Save Method", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    editingMethod = null
                    isNewMethod = false
                }) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Deposit & Payment Methods",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure TRON TRC20, Bitcoin, Ethereum and Bank routing",
                    color = Slate400,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { openEdit(null, true) },
                colors = ButtonDefaults.buttonColors(containerColor = TronGold),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("add_payment_method_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(paymentMethods) { pm ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (pm.network.contains("TRC20")) TronGold else ObsidianBorder)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(pm.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(pm.network, color = TronGold, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                            StatusBadge(if (pm.isActive) "ACTIVE" else "DISABLED")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Receiving Address / Account:", color = Slate400, fontSize = 10.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ObsidianSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = pm.walletAddressOrAccount,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Holder: ${pm.holderName}", color = Slate300, fontSize = 11.sp)
                        Text("Instructions: ${pm.instructions}", color = Slate400, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { openEdit(pm, false) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Settings", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
