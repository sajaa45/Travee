package com.example.travee.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.travee.data.ThemeManager

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1EBFC3),
    secondary = Color(0xFF4A6572),
    tertiary = Color(0xFF2D3B41),
    background = Color(0xFFF5F7F9),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1EBFC3),
    secondary = Color(0xFF4A6572),
    tertiary = Color(0xFF2D3B41),
    background = Color(0xFF121212),
    surface = Color(0xFF2D3B41),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun TravelAppTheme(
    themeManager: ThemeManager = ThemeManager.getInstance(),
    content: @Composable () -> Unit
) {
    val darkTheme by themeManager.isDarkTheme.collectAsState()
    val dynamicColor = false


    val colorScheme = when {
        dynamicColor && darkTheme -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        dynamicColor && !darkTheme -> {
            val context = LocalContext.current
            dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
