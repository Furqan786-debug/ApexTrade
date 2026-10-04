package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

data class Candle(
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Double
)

@Composable
fun TradingChart(
    symbol: String,
    currentPrice: Double,
    change24h: Double,
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf("15m") }
    var chartType by remember { mutableStateOf("Candles") } // "Candles" or "Line"
    val timeframes = listOf("1m", "5m", "15m", "1h", "4h", "1D")

    // Generate deterministic candles based on current price
    val candles = remember(symbol, currentPrice, selectedTimeframe) {
        generateCandlesForPrice(currentPrice, selectedTimeframe)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trading_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Symbol, Current Price, 24h Change, High/Low
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = symbol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (change24h >= 0) TradeGreenBg else TradeRedBg
                        ) {
                            Text(
                                text = "${if (change24h >= 0) "+" else ""}${change24h}%",
                                color = if (change24h >= 0) TradeGreen else TradeRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", currentPrice)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = if (change24h >= 0) TradeGreen else TradeRed
                    )
                }

                // Chart mode toggle
                Row(
                    modifier = Modifier
                        .background(ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    listOf("Candles", "Line").forEach { type ->
                        val isSelected = chartType == type
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) TradeGreen.copy(alpha = 0.2f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { chartType = type }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = type,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TradeGreen else Slate400
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timeframe selector bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                timeframes.forEach { tf ->
                    val isSelected = selectedTimeframe == tf
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) ObsidianBorder else Color.Transparent,
                        modifier = Modifier
                            .clickable { selectedTimeframe = tf }
                            .testTag("tf_$tf")
                    ) {
                        Text(
                            text = tf,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Slate500,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (candles.isEmpty()) return@Canvas

                    val minPrice = candles.minOf { it.low } * 0.998
                    val maxPrice = candles.maxOf { it.high } * 1.002
                    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.001)

                    val maxVolume = candles.maxOf { it.volume }.coerceAtLeast(1.0)
                    val candleCount = candles.size
                    val spacing = size.width / candleCount
                    val candleWidth = spacing * 0.65f

                    // 1. Draw horizontal grid lines & price labels
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = size.height * (i.toFloat() / gridLines) * 0.8f
                        drawLine(
                            color = ObsidianBorder.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                    }

                    if (chartType == "Candles") {
                        // 2. Draw Candlesticks & Volume
                        candles.forEachIndexed { index, c ->
                            val x = index * spacing + spacing / 2f
                            val isBullish = c.close >= c.open
                            val candleColor = if (isBullish) TradeGreen else TradeRed

                            // High/Low Wick
                            val yHigh = ((maxPrice - c.high) / priceRange).toFloat() * (size.height * 0.8f)
                            val yLow = ((maxPrice - c.low) / priceRange).toFloat() * (size.height * 0.8f)
                            drawLine(
                                color = candleColor,
                                start = Offset(x, yHigh),
                                end = Offset(x, yLow),
                                strokeWidth = 2f
                            )

                            // Open/Close Body
                            val yOpen = ((maxPrice - c.open) / priceRange).toFloat() * (size.height * 0.8f)
                            val yClose = ((maxPrice - c.close) / priceRange).toFloat() * (size.height * 0.8f)
                            val bodyTop = minOf(yOpen, yClose)
                            val bodyHeight = abs(yOpen - yClose).coerceAtLeast(2f)

                            drawRect(
                                color = candleColor,
                                topLeft = Offset(x - candleWidth / 2f, bodyTop),
                                size = Size(candleWidth, bodyHeight)
                            )

                            // Volume Bar (at the bottom 20% of canvas)
                            val volHeight = (c.volume / maxVolume).toFloat() * (size.height * 0.2f)
                            val volTop = size.height - volHeight
                            drawRect(
                                color = candleColor.copy(alpha = 0.35f),
                                topLeft = Offset(x - candleWidth / 2f, volTop),
                                size = Size(candleWidth, volHeight)
                            )
                        }
                    } else {
                        // Line Chart
                        val path = Path()
                        val fillPath = Path()

                        candles.forEachIndexed { index, c ->
                            val x = index * spacing + spacing / 2f
                            val y = ((maxPrice - c.close) / priceRange).toFloat() * (size.height * 0.8f)
                            if (index == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, size.height * 0.8f)
                                fillPath.lineTo(x, y)
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                            if (index == candles.size - 1) {
                                fillPath.lineTo(x, size.height * 0.8f)
                                fillPath.close()
                            }
                        }

                        drawPath(
                            path = fillPath,
                            color = TradeGreen.copy(alpha = 0.12f)
                        )
                        drawPath(
                            path = path,
                            color = TradeGreen,
                            style = Stroke(width = 3f)
                        )
                    }

                    // Current live price pulse line
                    val currentY = ((maxPrice - currentPrice) / priceRange).toFloat() * (size.height * 0.8f)
                    drawLine(
                        color = TradeGreenLight,
                        start = Offset(0f, currentY),
                        end = Offset(size.width, currentY),
                        strokeWidth = 1.5f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawCircle(
                        color = TradeGreenLight,
                        radius = 4.dp.toPx(),
                        center = Offset(size.width - 6.dp.toPx(), currentY)
                    )
                }
            }
        }
    }
}

private fun generateCandlesForPrice(current: Double, tf: String): List<Candle> {
    val count = 24
    val candles = mutableListOf<Candle>()
    var prev = current * 0.97
    val volatility = current * 0.006

    for (i in 0 until count - 1) {
        val open = prev
        val change = (Random.nextDouble() - 0.48) * volatility
        val close = open + change
        val high = maxOf(open, close) + Random.nextDouble() * (volatility * 0.5)
        val low = minOf(open, close) - Random.nextDouble() * (volatility * 0.5)
        val vol = Random.nextDouble(50000.0, 300000.0)
        candles.add(Candle(open, close, high, low, vol))
        prev = close
    }

    // Last candle finishes on current live price
    val lastOpen = prev
    val lastClose = current
    val lastHigh = maxOf(lastOpen, lastClose) + (volatility * 0.2)
    val lastLow = minOf(lastOpen, lastClose) - (volatility * 0.2)
    candles.add(Candle(lastOpen, lastClose, lastHigh, lastLow, 185000.0))

    return candles
}
