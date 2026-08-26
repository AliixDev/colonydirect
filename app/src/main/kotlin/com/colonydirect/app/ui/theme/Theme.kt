package com.colonydirect.app.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ColonyLightScheme = lightColorScheme(
    primary         = ColonyGreen700,
    onPrimary       = ColonyWhite,
    primaryContainer= ColonyGreen50,
    onPrimaryContainer = ColonyGreen900,

    secondary       = ColonyAmber500,
    onSecondary     = ColonyNeutral900,
    secondaryContainer = ColonyAmber100,
    onSecondaryContainer = ColonyAmber700,

    background      = ColonyWhite,
    onBackground    = ColonyNeutral900,
    surface         = ColonyNeutral100,
    onSurface       = ColonyNeutral900,
    surfaceVariant  = ColonyGreen50,
    onSurfaceVariant= ColonyNeutral600,
    outline         = ColonyNeutral300,

    error           = ColonyError,
    onError         = ColonyWhite,
    errorContainer  = ColonyErrorLight,
    onErrorContainer= ColonyError
)

private val ColonyDarkScheme = darkColorScheme(
    primary         = ColonyGreen200,
    onPrimary       = ColonyGreen900,
    primaryContainer= ColonyGreen700,
    onPrimaryContainer = ColonyGreen50,

    secondary       = ColonyAmber500,
    onSecondary     = ColonyNeutral900,

    background      = Color(0xFF1A1C1A),
    onBackground    = ColonyNeutral100,
    surface         = Color(0xFF1A1C1A),
    onSurface       = ColonyNeutral100
)

@Composable
fun ColonyDirectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // keep brand identity consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ColonyDarkScheme
        else      -> ColonyLightScheme
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
        typography  = ColonyTypography,
        content     = content
    )
}
