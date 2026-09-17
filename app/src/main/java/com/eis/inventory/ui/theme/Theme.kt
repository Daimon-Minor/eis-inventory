package com.eis.inventory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F6E68),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB6E7E2),
    onPrimaryContainer = Color(0xFF00201E),
    secondary = Color(0xFFB26A00),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDDB4),
    onSecondaryContainer = Color(0xFF2A1700),
    tertiary = Color(0xFF2F5DA8),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F7F8),
    onBackground = Color(0xFF191C1C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFE4EAEA),
    onSurfaceVariant = Color(0xFF48504F),
    outline = Color(0xFF798382),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FD8D1),
    onPrimary = Color(0xFF003733),
    primaryContainer = Color(0xFF00504B),
    onPrimaryContainer = Color(0xFFA3F2EA),
    secondary = Color(0xFFF2BE7B),
    onSecondary = Color(0xFF462A00),
    secondaryContainer = Color(0xFF653D00),
    onSecondaryContainer = Color(0xFFFFDDB4),
    tertiary = Color(0xFFA8C8FF),
    onTertiary = Color(0xFF00315F),
    background = Color(0xFF0F1414),
    onBackground = Color(0xFFDEE4E3),
    surface = Color(0xFF171D1D),
    onSurface = Color(0xFFDEE4E3),
    surfaceVariant = Color(0xFF3F4948),
    onSurfaceVariant = Color(0xFFBEC9C7),
    outline = Color(0xFF899392),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

@Composable
fun EisTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
