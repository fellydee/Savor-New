package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Savor Brand Colors
val SavorAccentOrange = Color(0xFFF25A19) // hsl(22 93% 55%)
val SavorGoldenYellow = Color(0xFFFFB800) // hsl(43 100% 50%)
val SavorCrimson = Color(0xFFD03030)      // hsl(0 63% 50%)
val SavorEarthyBrown = Color(0xFF7D3B34)  // hsl(5 45% 34%)

// Brand Gradients
val SavorGradientBrush = Brush.horizontalGradient(
    colors = listOf(SavorAccentOrange, SavorGoldenYellow)
)
val SavorVerticalGradient = Brush.verticalGradient(
    colors = listOf(SavorAccentOrange, SavorGoldenYellow)
)
val SavorStoryRingGradient = Brush.sweepGradient(
    colors = listOf(
        SavorAccentOrange,
        SavorCrimson,
        SavorGoldenYellow,
        SavorAccentOrange
    )
)

// Light Theme Palette (Warm Cream surfaces)
val CreamBackground = Color(0xFFFBF8F3)
val CreamSurface = Color(0xFFFFFFFF)
val CreamSurfaceVariant = Color(0xFFF4ECE4)
val CreamBorder = Color(0xFFE9DFD5)
val TextPrimaryLight = Color(0xFF2C2420)
val TextSecondaryLight = Color(0xFF786B63)
val TextTertiaryLight = Color(0xFFA5978E)

// Dark Theme Palette
val DarkBackground = Color(0xFF141110)
val DarkSurface = Color(0xFF1E1917)
val DarkSurfaceVariant = Color(0xFF2B231F)
val DarkBorder = Color(0xFF3B312B)
val TextPrimaryDark = Color(0xFFF8F5F2)
val TextSecondaryDark = Color(0xFFB5ABA3)
val TextTertiaryDark = Color(0xFF81756D)

// Badges & Accents
val VerifiedBadgeColor = Color(0xFF2196F3)
val ChefFlowerGold = Color(0xFFFFC107)
val ChefFlowerPink = Color(0xFFFF5252)
val SuccessGreen = Color(0xFF4CAF50)
