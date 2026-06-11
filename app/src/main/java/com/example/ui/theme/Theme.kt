package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RideNeonCyan,
    secondary = RideNeonBlue,
    tertiary = AccentTeal,
    background = DeepSlateBackground,
    surface = DeepSlateBackground,
    onPrimary = DeepSlateBackground,
    onSecondary = TextPrimaryGlow,
    onBackground = TextPrimaryGlow,
    onSurface = TextPrimaryGlow
)

private val LightColorScheme = lightColorScheme(
    primary = RideNeonBlue,
    secondary = RideNeonCyan,
    tertiary = AccentTeal,
    background = TextPrimaryGlow,
    surface = TextPrimaryGlow,
    onPrimary = TextPrimaryGlow,
    onSecondary = DeepSlateBackground,
    onBackground = DeepSlateBackground,
    onSurface = DeepSlateBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark mode first for luxury tech feel
    dynamicColor: Boolean = false, // Disable dynamic colors to keep RideShield premium style
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
