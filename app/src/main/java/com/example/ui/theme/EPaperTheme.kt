package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Typography Engine: Custom High-Legibility Serif Typography
val EPaperFontFamily = FontFamily.Serif

val EPaperTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = EPaperFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)

// Day Mode (Paper White & Warm Cream)
val PaperWhiteBackground = Color(0xFFF4F1EA)
val PaperWhiteSurface = Color(0xFFEAE6DF)
val PaperWhiteOnBackground = Color(0xFF111111)
val PaperWhiteOnSurface = Color(0xFF1A1A1A)
val PaperWhiteSecondaryText = Color(0xFF444444)
val PaperWhiteBorder = Color(0xFFD2CFC7)

// Night Mode (Inverted E-Ink Warm Amber / Low Blue Light)
val AmberNightBackground = Color(0xFF050505)
val AmberNightSurface = Color(0xFF181818)
val AmberNightOnBackground = Color(0xFFD4A373)
val AmberNightOnSurface = Color(0xFFE2B282)
val AmberNightSecondaryText = Color(0xFFA08060)
val AmberNightBorder = Color(0xFF332A20)

private val EPaperDayColorScheme = lightColorScheme(
    primary = PaperWhiteOnBackground,
    onPrimary = PaperWhiteBackground,
    background = PaperWhiteBackground,
    onBackground = PaperWhiteOnBackground,
    surface = PaperWhiteSurface,
    onSurface = PaperWhiteOnSurface,
    surfaceVariant = PaperWhiteBorder,
    onSurfaceVariant = PaperWhiteSecondaryText
)

private val EPaperNightColorScheme = darkColorScheme(
    primary = AmberNightOnBackground,
    onPrimary = AmberNightBackground,
    background = AmberNightBackground,
    onBackground = AmberNightOnBackground,
    surface = AmberNightSurface,
    onSurface = AmberNightOnSurface,
    surfaceVariant = AmberNightBorder,
    onSurfaceVariant = AmberNightSecondaryText
)

@Composable
fun EPaperTheme(
    isNightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isNightMode) EPaperNightColorScheme else EPaperDayColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EPaperTypography,
        content = content
    )
}
