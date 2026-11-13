package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryRed,
    onPrimary = Color.White,
    primaryContainer = DarkPrimaryRedContainer,
    onPrimaryContainer = Color(0xFFFFDADA),
    secondary = DarkAccentGold,
    onSecondary = Color.Black,
    secondaryContainer = DarkSecondaryNavyContainer,
    onSecondaryContainer = Color(0xFFF1F5F9),
    tertiary = SuccessGreen,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = Color(0xFF374151)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = PrimaryRedContainer,
    onPrimaryContainer = OnPrimaryRedContainer,
    secondary = SecondaryNavy,
    onSecondary = Color.White,
    secondaryContainer = SecondaryNavyContainer,
    onSecondaryContainer = SecondaryNavy,
    tertiary = AccentGold,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = Color(0xFFCBD5E1)
)

@Composable
fun CrediPulseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
