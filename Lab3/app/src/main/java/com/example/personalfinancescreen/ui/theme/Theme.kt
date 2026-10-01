package com.example.personalfinancescreen.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = FinanceBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = FinanceBlueLight,
    onPrimaryContainer = FinanceBlue,
    secondary = FinanceGreen,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = FinanceGreenLight,
    onSecondaryContainer = FinanceGreen,
    tertiary = FinanceGreen,
    error = FinanceRed,
    errorContainer = FinanceRedLight,
    onErrorContainer = FinanceRed,
    background = LightBackground,

    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFF1A1B20),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF45474F)
)

private val DarkColors = darkColorScheme(
    primary = ColorLightBlue,
    onPrimary = androidx.compose.ui.graphics.Color(0xFF002E69),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF174889),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFD7E3FF),
    secondary = FinanceGreenLight,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF003921),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF005332),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFB8F3D3),
    error = androidx.compose.ui.graphics.Color(0xFFFFB4AB),
    errorContainer = androidx.compose.ui.graphics.Color(0xFF93000A),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),
    background = DarkBackground,

    surface = androidx.compose.ui.graphics.Color(0xFF111318),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE2E2E9),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC5C6D0)
)

private val AppTypography = Typography()

@Composable
fun FinanceTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) {
            DarkColors
        } else {
            LightColors
        }, typography = AppTypography, content = content
    )
}