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

// Typography Engine: Custom High-Legibility Serif & Mono Typography for E-Paper
val EPaperFontFamily = FontFamily.Serif
val EPaperMonoFamily = FontFamily.Monospace

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

/**
 * E-Paper Contrast & Warmth Profiles
 */
enum class EPaperColorProfile(val label: String, val badgeText: String) {
    PAPER_WHITE("Paper White (Auto)", "PAPER WHITE"),
    WARM_AMBER("Warm Amber", "WARM AMBER"),
    SLATE_CHARCOAL("Slate Charcoal", "SLATE CHARCOAL"),
    HIGH_CONTRAST("1-Bit Monochrome", "HIGH CONTRAST");

    fun next(): EPaperColorProfile {
        val entries = entries
        val nextIndex = (ordinal + 1) % entries.size
        return entries[nextIndex]
    }
}

// 1. Paper White (Day Warm Cream & Rich Charcoal Ink)
val PaperWhiteBackground = Color(0xFFF5F2EB)
val PaperWhiteSurface = Color(0xFFEBE6DC)
val PaperWhiteOnBackground = Color(0xFF141414)
val PaperWhiteOnSurface = Color(0xFF1F1F1F)
val PaperWhiteSecondaryText = Color(0xFF4A4A4A)
val PaperWhiteBorder = Color(0xFFD5D0C5)

// 2. Warm Amber (Night Inverted Low-Blue-Light)
val AmberNightBackground = Color(0xFF070605)
val AmberNightSurface = Color(0xFF1A1612)
val AmberNightOnBackground = Color(0xFFE0B080)
val AmberNightOnSurface = Color(0xFFE8BC8E)
val AmberNightSecondaryText = Color(0xFFA68560)
val AmberNightBorder = Color(0xFF382C20)

// 3. Slate Charcoal (Muted Deep Dark)
val SlateBackground = Color(0xFF121418)
val SlateSurface = Color(0xFF1E2128)
val SlateOnBackground = Color(0xFFE8EBF0)
val SlateOnSurface = Color(0xFFDFE3EB)
val SlateSecondaryText = Color(0xFF9098A8)
val SlateBorder = Color(0xFF2C3240)

// 4. High-Contrast 1-Bit Monochrome
val MonoBackground = Color(0xFFFFFFFF)
val MonoSurface = Color(0xFFF0F0F0)
val MonoOnBackground = Color(0xFF000000)
val MonoOnSurface = Color(0xFF000000)
val MonoSecondaryText = Color(0xFF333333)
val MonoBorder = Color(0xFF000000)

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

private val EPaperAmberColorScheme = darkColorScheme(
    primary = AmberNightOnBackground,
    onPrimary = AmberNightBackground,
    background = AmberNightBackground,
    onBackground = AmberNightOnBackground,
    surface = AmberNightSurface,
    onSurface = AmberNightOnSurface,
    surfaceVariant = AmberNightBorder,
    onSurfaceVariant = AmberNightSecondaryText
)

private val EPaperSlateColorScheme = darkColorScheme(
    primary = SlateOnBackground,
    onPrimary = SlateBackground,
    background = SlateBackground,
    onBackground = SlateOnBackground,
    surface = SlateSurface,
    onSurface = SlateOnSurface,
    surfaceVariant = SlateBorder,
    onSurfaceVariant = SlateSecondaryText
)

private val EPaperMonoColorScheme = lightColorScheme(
    primary = MonoOnBackground,
    onPrimary = MonoBackground,
    background = MonoBackground,
    onBackground = MonoOnBackground,
    surface = MonoSurface,
    onSurface = MonoOnSurface,
    surfaceVariant = MonoBorder,
    onSurfaceVariant = MonoSecondaryText
)

@Composable
fun EPaperTheme(
    profile: EPaperColorProfile = EPaperColorProfile.PAPER_WHITE,
    content: @Composable () -> Unit
) {
    val colorScheme = when (profile) {
        EPaperColorProfile.PAPER_WHITE -> EPaperDayColorScheme
        EPaperColorProfile.WARM_AMBER -> EPaperAmberColorScheme
        EPaperColorProfile.SLATE_CHARCOAL -> EPaperSlateColorScheme
        EPaperColorProfile.HIGH_CONTRAST -> EPaperMonoColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EPaperTypography,
        content = content
    )
}

@Composable
fun EPaperTheme(
    isNightMode: Boolean,
    content: @Composable () -> Unit
) {
    EPaperTheme(
        profile = if (isNightMode) EPaperColorProfile.WARM_AMBER else EPaperColorProfile.PAPER_WHITE,
        content = content
    )
}
