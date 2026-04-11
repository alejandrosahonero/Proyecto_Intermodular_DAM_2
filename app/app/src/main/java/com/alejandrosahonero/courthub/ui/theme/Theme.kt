package com.alejandrosahonero.courthub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CourtHubColorScheme = darkColorScheme(
    primary = Red600,
    onPrimary = OnPrimary,
    primaryContainer = RedDark,
    onPrimaryContainer = RedLight,

    background = Background,
    onBackground = TextPrimary,

    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = Error,
    onError = OnPrimary,

    outline = Outline,
    outlineVariant = Divider
)

@Composable
fun CourtHubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CourtHubColorScheme,
        typography = CourtHubTypography,
        content = content
    )
}