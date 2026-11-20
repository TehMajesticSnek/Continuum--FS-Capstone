package com.continuum.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ContinuumColorScheme = darkColorScheme(
    primary = BluePrimary,
    secondary = CyanAccent,
    tertiary = CyanAccent,

    background = NavyBackground,
    surface = Surface,

    onPrimary = PrimaryText,
    onSecondary = NavyBackground,
    onTertiary = NavyBackground,

    onBackground = PrimaryText,
    onSurface = PrimaryText,

    outline = Border,
    error = Critical,
    onError = PrimaryText
)

@Composable
fun ContinuumTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ContinuumColorScheme,
        typography = Typography,
        content = content
    )
}