package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.TronGold
import com.example.ui.theme.TronRed
import kotlin.math.abs

/**
 * A crisp, self-contained QR code visualizer designed for crypto wallet addresses
 * including deterministic finder patterns and matrix blocks derived from the address.
 */
@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 200,
    networkBadge: String = "TRON (TRC20)"
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(sizeDp.dp)
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridSize = 25
                    val cellSize = size.width / gridSize
                    val darkColor = Color(0xFF0F172A)

                    // Draw Finder Pattern helper
                    fun drawFinder(startX: Int, startY: Int) {
                        // Outer 7x7
                        drawRect(
                            color = darkColor,
                            topLeft = Offset(startX * cellSize, startY * cellSize),
                            size = Size(7 * cellSize, 7 * cellSize)
                        )
                        // Inner 5x5 white
                        drawRect(
                            color = Color.White,
                            topLeft = Offset((startX + 1) * cellSize, (startY + 1) * cellSize),
                            size = Size(5 * cellSize, 5 * cellSize)
                        )
                        // Center 3x3 black
                        drawRect(
                            color = darkColor,
                            topLeft = Offset((startX + 2) * cellSize, (startY + 2) * cellSize),
                            size = Size(3 * cellSize, 3 * cellSize)
                        )
                    }

                    // Top-Left Finder
                    drawFinder(0, 0)
                    // Top-Right Finder
                    drawFinder(gridSize - 7, 0)
                    // Bottom-Left Finder
                    drawFinder(0, gridSize - 7)

                    // Deterministic pseudorandom modules based on address hash
                    val hash = abs(data.hashCode())
                    for (r in 0 until gridSize) {
                        for (c in 0 until gridSize) {
                            // Skip finder pattern zones
                            val inTL = r < 8 && c < 8
                            val inTR = r < 8 && c >= gridSize - 8
                            val inBL = r >= gridSize - 8 && c < 8
                            val inCenterBadge = r in 10..14 && c in 10..14

                            if (!inTL && !inTR && !inBL && !inCenterBadge) {
                                val bit = ((hash * (r + 1) * 31 + c * 17 + (r xor c)) % 3) == 0
                                if (bit || (r % 2 == 0 && c % 4 == 0)) {
                                    drawRect(
                                        color = darkColor,
                                        topLeft = Offset(c * cellSize, r * cellSize),
                                        size = Size(cellSize * 0.92f, cellSize * 0.92f)
                                    )
                                }
                            }
                        }
                    }

                    // Center badge background
                    drawCircle(
                        color = Color.White,
                        radius = cellSize * 2.8f,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                    // Center network badge
                    drawCircle(
                        color = TronRed,
                        radius = cellSize * 2.3f,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                }

                // Text overlay in center of QR
                Text(
                    text = "TRX",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = TronGold.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TronGold.copy(alpha = 0.5f)))
        ) {
            Text(
                text = networkBadge,
                color = TronGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}
