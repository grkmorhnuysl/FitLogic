@file:Suppress("FunctionName")

package com.fitlogic.ai.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme =
    lightColorScheme(
        primary = FlBlue,
        secondary = FlGreen,
        surface = FlSurfaceLight,
        onSurface = FlOnLight,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = FlBlueDark,
        secondary = FlGreen,
        surface = FlSurfaceDark,
        onSurface = FlOnDark,
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
