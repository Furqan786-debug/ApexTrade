package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingViewModel

@Composable
fun AuthScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Trader Login, 1: Sign Up

    // Trader Login Inputs
    var loginEmail by remember { mutableStateOf("trader_john") }
    var loginPassword by remember { mutableStateOf("trader123") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Sign Up Inputs pre-filled with user details so new accounts immediately have full profile
    var regUsername by remember { mutableStateOf("Furqan Raees") }
    var regEmail by remember { mutableStateOf("ffurqanraees@gmail.com") }
    var regPhone by remember { mutableStateOf("+92 300 1234567") }
    var regPassword by remember { mutableStateOf("Trader@123") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Brand Header: ApexTrade
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianSurfaceVariant,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(TradeGreen)
                        ),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "A",
                                color = TradeGreen,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ApexTrade",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Global Financial Trading Platform",
                        color = Slate300,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(TradeGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Centralized Real-Time Backend Active",
                            color = Slate400,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Tab Selector: Login / Sign Up only (Admin Portal removed from front)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianSurface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        // Trader Login Tab
                        TabPill(
                            title = "Login",
                            isSelected = selectedTab == 0,
                            activeColor = TradeGreen,
                            activeTextColor = Color.Black,
                            modifier = Modifier.weight(1f)
                        ) { selectedTab = 0 }

                        // Register Tab
                        TabPill(
                            title = "Sign Up",
                            isSelected = selectedTab == 1,
                            activeColor = TradeGreen,
                            activeTextColor = Color.Black,
                            modifier = Modifier.weight(1f)
                        ) { selectedTab = 1 }
                    }
                }
            }

            // Content per Tab
            when (selectedTab) {
                // TAB 0: TRADER LOGIN
                0 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = TradeGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Trader Sign In", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = { loginEmail = it },
                                    label = { Text("Username or Email") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate400) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_username_field")
                                )

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Slate400) },
                                    trailingIcon = {
                                        IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                            Icon(
                                                imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password visibility",
                                                tint = Slate400
                                            )
                                        }
                                    },
                                    visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_field")
                                )

                                Button(
                                    onClick = {
                                        viewModel.loginUser(loginEmail, loginPassword)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("login_submit_btn")
                                ) {
                                    Text("Log In to Trading", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                // TAB 1: SIGN UP (NEW ACCOUNT)
                1 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = TradeGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Create Trader Account", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                // Zero Balance notice from user requirements
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ObsidianSurfaceVariant,
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(TronGold.copy(alpha = 0.5f))
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = TronGold, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "New Account Policy: Balance starts strictly at 0.00 USDT until you make a deposit. Trading remains locked until deposit.",
                                            color = Slate300,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = regUsername,
                                    onValueChange = { regUsername = it },
                                    label = { Text("Username") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate400) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_username_field")
                                )

                                OutlinedTextField(
                                    value = regEmail,
                                    onValueChange = { regEmail = it },
                                    label = { Text("Email Address") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Slate400) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_email_field")
                                )

                                OutlinedTextField(
                                    value = regPhone,
                                    onValueChange = { regPhone = it },
                                    label = { Text("Phone Number") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Slate400) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_phone_field")
                                )

                                OutlinedTextField(
                                    value = regPassword,
                                    onValueChange = { regPassword = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Slate400) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_password_field")
                                )

                                Button(
                                    onClick = {
                                        if (regUsername.isNotBlank() && regEmail.isNotBlank()) {
                                            viewModel.registerNewAccount(
                                                username = regUsername.trim(),
                                                email = regEmail.trim(),
                                                phone = regPhone.trim().ifBlank { "+1 (555) 000-0000" }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TradeGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("reg_submit_btn")
                                ) {
                                    Text("Create Account", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    activeTextColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) activeColor else Color.Transparent,
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(
                text = title,
                color = if (isSelected) activeTextColor else Slate400,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}
