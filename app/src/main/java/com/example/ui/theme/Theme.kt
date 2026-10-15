package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SavorDarkColorScheme = darkColorScheme(
    primary = SavorAccentOrange,
    onPrimary = Color.White,
    primaryContainer = SavorEarthyBrown,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = SavorGoldenYellow,
    onSecondary = Color(0xFF332000),
    secondaryContainer = Color(0xFF4E3700),
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = SavorCrimson,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder
)

private val SavorLightColorScheme = lightColorScheme(
    primary = SavorAccentOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDE5),
    onPrimaryContainer = Color(0xFF6B1D00),
    secondary = SavorGoldenYellow,
    onSecondary = Color(0xFF3B2800),
    secondaryContainer = Color(0xFFFFF3CD),
    onSecondaryContainer = Color(0xFF5D4200),
    tertiary = SavorCrimson,
    onTertiary = Color.White,
    background = CreamBackground,
    onBackground = TextPrimaryLight,
    surface = CreamSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = CreamBorder
)

@Composable
fun SavorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SavorDarkColorScheme else SavorLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
