package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SiloDarkColorScheme = darkColorScheme(
    primary = SiloBrandBlueLight,
    onPrimary = ElegantDarkOnPrimary,
    primaryContainer = SiloBrandContainer,
    onPrimaryContainer = SiloOnBrandContainer,
    secondary = SiloBrandBlueLight,
    onSecondary = ElegantDarkOnPrimary,
    secondaryContainer = SiloDarkSurfaceVariant,
    onSecondaryContainer = SiloDarkOnSurface,
    background = SiloDarkBg,
    onBackground = SiloDarkOnSurface,
    surface = SiloDarkSurface,
    onSurface = SiloDarkOnSurface,
    surfaceVariant = SiloDarkSurfaceVariant,
    onSurfaceVariant = SiloDarkOnSurfaceVariant,
    outline = SiloDarkOutline,
    outlineVariant = SiloDarkSubtleBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SiloDarkColorScheme,
        typography = Typography,
        content = content
    )
}
