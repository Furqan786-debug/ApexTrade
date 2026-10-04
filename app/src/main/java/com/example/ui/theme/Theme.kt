package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TradeGreen,
    onPrimary = Color.Black,
    primaryContainer = TradeGreenBg,
    onPrimaryContainer = TradeGreenLight,
    secondary = TronGold,
    onSecondary = Color.Black,
    secondaryContainer = TronGoldBg,
    onSecondaryContainer = TronGold,
    tertiary = CyanAccent,
    background = ObsidianBg,
    onBackground = Color(0xFFF1F5F9),
    surface = ObsidianSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = Slate400,
    outline = ObsidianBorder,
    error = TradeRed,
    onError = Color.White,
    errorContainer = TradeRedBg,
    onErrorContainer = TradeRedLight
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF047857),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = Color(0xFF0284C7),
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    error = TradeRed,
    onError = Color.White,
    errorContainer = TradeRedBg,
    onErrorContainer = TradeRed
)

@Composable
fun ApexTradeTheme(
    darkTheme: Boolean = true, // Default to sleek pro fintech dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
