package com.patil.coconut_pfa.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.patil.coconut_pfa.data.local.datastore.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Emerald80,
    onPrimary = Color(0xFF003732),
    primaryContainer = EmeraldPrimaryContainerDark,
    onPrimaryContainer = Color(0xFFB0F2EA),
    secondary = SlateSecondary80,
    onSecondary = Color(0xFF1E293B),
    secondaryContainer = SlateSecondaryContainerDark,
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = AmberTertiary80,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = AmberTertiaryContainerDark,
    onTertiaryContainer = Color(0xFFFDE68A),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurface,
    surfaceContainerLow = DarkBackground,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerHigh = DarkSurface,
    surfaceContainerHighest = DarkSurfaceVariant,
    surfaceTint = Color.Transparent
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald40,
    onPrimary = Color.White,
    primaryContainer = EmeraldPrimaryContainerLight,
    onPrimaryContainer = Color(0xFF042F2C),
    secondary = SlateSecondary40,
    onSecondary = Color.White,
    secondaryContainer = SlateSecondaryContainerLight,
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = AmberTertiary40,
    onTertiary = Color.White,
    tertiaryContainer = AmberTertiaryContainerLight,
    onTertiaryContainer = Color(0xFF451A03),
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainer = LightSurface,
    surfaceContainerLow = LightBackground,
    surfaceContainerLowest = LightSurface,
    surfaceContainerHigh = LightSurface,
    surfaceContainerHighest = LightSurfaceVariant,
    surfaceTint = Color.Transparent
)

@Composable
fun Coconut_PfaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
