package com.example.ui.theme

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

private val AmoledColorScheme = darkColorScheme(
    primary = AuraPrimaryVariant,
    onPrimary = Color.Black,
    secondary = AuraSecondary,
    onSecondary = Color.Black,
    tertiary = AuraAccent,
    background = AmoledBg,
    surface = AmoledSurface,
    surfaceVariant = AmoledCard,
    onBackground = Color(0xFFF0F1F5),
    onSurface = Color(0xFFF0F1F5),
    onSurfaceVariant = Color(0xFFB0B4C3)
)

private val NeonColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = NeonPurple,
    onSecondary = Color.White,
    tertiary = NeonPink,
    background = Color(0xFF07080D),
    surface = Color(0xFF0F111A),
    surfaceVariant = Color(0xFF1B1E2E),
    onBackground = Color(0xFFF8F9FA),
    onSurface = Color(0xFFF8F9FA)
)

private val LightColorScheme = lightColorScheme(
    primary = AuraPrimary,
    onPrimary = Color.White,
    secondary = AuraSecondary,
    onSecondary = Color.White,
    tertiary = AuraAccent,
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFEDEFF5),
    onBackground = Color(0xFF1A1C20),
    onSurface = Color(0xFF1A1C20),
    onSurfaceVariant = Color(0xFF5E6272)
)

@Composable
fun AuraLauncherTheme(
    themeMode: String = "AMOLED",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when (themeMode) {
        "AMOLED" -> AmoledColorScheme
        "NEON" -> NeonColorScheme
        "LIGHT" -> LightColorScheme
        "SYSTEM" -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else if (darkTheme) {
                AmoledColorScheme
            } else {
                LightColorScheme
            }
        }
        else -> AmoledColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
