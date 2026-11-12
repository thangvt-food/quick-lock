package com.quicklock.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrandBlue = Color(0xFF1B6EF3)

internal val StatusOk = Color(0xFF22C55E)

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    secondary = BrandBlue,
    background = Color(0xFFF5F7FB),
    onBackground = Color(0xFF0F1720),
    surface = Color.White,
    onSurface = Color(0xFF0F1720),
    surfaceVariant = Color(0xFFE9EEF7),
    onSurfaceVariant = Color(0xFF46536B),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4FF),
    onPrimary = Color(0xFF00214D),
    secondary = Color(0xFF8AB4FF),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFE7ECF3),
    surface = Color(0xFF141A22),
    onSurface = Color(0xFFE7ECF3),
    surfaceVariant = Color(0xFF1C2532),
    onSurfaceVariant = Color(0xFFAFBCCF),
    outline = Color(0xFF37414F),
    error = Color(0xFFF87171),
)

@Composable
fun QuickLockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
