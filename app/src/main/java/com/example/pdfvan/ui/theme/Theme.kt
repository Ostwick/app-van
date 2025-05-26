package com.example.pdfvan.ui.theme // Adjust package if needed

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

// Using BrandBlue as Primary and BrandGreen as Secondary
private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = White,
    primaryContainer = BrandBlue,
    onPrimaryContainer = White,

    secondary = BrandGreen,
    onSecondary = White,
    secondaryContainer = BrandGreen,
    onSecondaryContainer = White,

    tertiary = BrandGreen,
    onTertiary = White,

    error = ErrorRed,
    onError = White,

    background = LightGray, // Light background for the app
    onBackground = Black,   // Text on light background

    surface = White,        // Card backgrounds, App Bar background
    onSurface = Black,      // Text on cards, App Bar title

    surfaceVariant = Color(0xFFE0E0E0), // For subtle variants like dividers or outlined field borders
    onSurfaceVariant = Color(0xFF49454F)
)


private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = White,
    primaryContainer = BrandBlue,
    onPrimaryContainer = White,

    secondary = BrandGreen,
    onSecondary = Black,
    secondaryContainer = BrandGreen,
    onSecondaryContainer = Black,

    tertiary = BrandGreen,
    onTertiary = Black,

    error = Color(0xFFCF6679),
    onError = Black,

    background = DarkGray,
    onBackground = White,

    surface = Color(0xFF1E1E1E),
    onSurface = White,

    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

@Composable
fun PdfVanTheme( // Changed from MyProjectTheme or similar
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to true if you want dynamic colors on Android 12+
    // and it will override your custom theme.
    // For strict branding, keep it false.
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Brand color for status bar
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Use the typography we defined
        content = content
    )
}