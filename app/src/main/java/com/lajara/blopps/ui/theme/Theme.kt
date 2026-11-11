package com.lajara.blopps.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BloppsMorado,
    onPrimary = Color.White,
    primaryContainer = BloppsSuperficieAltaDark,
    onPrimaryContainer = BloppsMoradoClaro,
    secondary = BloppsDorado,
    onSecondary = Color(0xFF1B1300),
    secondaryContainer = Color.Transparent,
    onSecondaryContainer = BloppsMoradoClaro,
    tertiary = BloppsMoradoClaro,
    background = BloppsFondoDark,
    onBackground = BloppsTextoPrincipalDark,
    surface = BloppsSuperficieDark,
    onSurface = BloppsTextoPrincipalDark,
    surfaceVariant = BloppsSuperficieAltaDark,
    onSurfaceVariant = BloppsTextoSecundarioDark,
    outline = BloppsBordeDark,
    outlineVariant = BloppsBordeDark,
    error = BloppsError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BloppsMorado,
    onPrimary = Color.White,
    primaryContainer = BloppsSuperficieAltaLight,
    onPrimaryContainer = BloppsMorado,
    secondary = BloppsDorado,
    onSecondary = Color(0xFF1B1300),
    secondaryContainer = Color.Transparent,
    onSecondaryContainer = BloppsMorado,
    tertiary = BloppsMorado,
    background = BloppsFondoLight,
    onBackground = BloppsTextoPrincipalLight,
    surface = BloppsSuperficieLight,
    onSurface = BloppsTextoPrincipalLight,
    surfaceVariant = BloppsSuperficieAltaLight,
    onSurfaceVariant = BloppsTextoSecundarioLight,
    outline = BloppsBordeLight,
    outlineVariant = BloppsBordeLight,
    error = BloppsError,
    onError = Color.White
)

@Composable
fun BloppsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
