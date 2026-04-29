@file:Suppress("FunctionName")

package com.fitlogic.ai.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
    darkColorScheme(
        primary = FlPrimary,
        onPrimary = Color.White,
        primaryContainer = FlPrimaryContainer,
        onPrimaryContainer = FlOnPrimaryContainer,
        secondary = FlSecondary,
        onSecondary = Color.White,
        secondaryContainer = FlSecondaryContainer,
        onSecondaryContainer = FlOnSecondaryContainer,
        tertiary = FlTertiary,
        onTertiary = Color.White,
        tertiaryContainer = FlTertiaryContainer,
        onTertiaryContainer = FlOnTertiaryContainer,
        background = FlBackgroundDark,
        onBackground = FlOnSurfaceDark,
        surface = FlSurfaceDark,
        onSurface = FlOnSurfaceDark,
        surfaceVariant = FlSurfaceVariantDark,
        onSurfaceVariant = FlOnSurfaceVariantDark,
        error = FlError,
        onError = FlOnError,
        errorContainer = FlErrorContainer,
        onErrorContainer = FlOnErrorContainer,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = FlPrimaryDark,
        onPrimary = Color.White,
        primaryContainer = FlOnPrimaryContainer,
        onPrimaryContainer = FlPrimaryDark,
        secondary = FlSecondaryDark,
        onSecondary = Color.White,
        secondaryContainer = FlOnSecondaryContainer,
        onSecondaryContainer = FlSecondaryDark,
        tertiary = FlTertiary,
        onTertiary = Color.White,
        tertiaryContainer = FlOnTertiaryContainer,
        onTertiaryContainer = FlTertiaryContainer,
        background = FlBackgroundLight,
        onBackground = FlOnSurfaceLight,
        surface = FlSurfaceLight,
        onSurface = FlOnSurfaceLight,
        surfaceVariant = FlSurfaceVariantLight,
        onSurfaceVariant = FlOnSurfaceVariantLight,
        error = FlPrimaryDark,
        onError = Color.White,
    )

@Composable
fun FitLogicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = FlTypography,
        shapes = FlShapes,
        content = content,
    )
}
