package com.lajara.blopps.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaBlopps = darkColorScheme(
    primary = BloppsMorado,
    onPrimary = Color.White,
    primaryContainer = BloppsSuperficieAlta,
    onPrimaryContainer = BloppsMoradoClaro,
    secondary = BloppsDorado,
    onSecondary = Color(0xFF1B1300),
    secondaryContainer = Color.Transparent,
    onSecondaryContainer = BloppsMoradoClaro,
    tertiary = BloppsMoradoClaro,
    background = BloppsFondo,
    onBackground = BloppsTextoPrincipal,
    surface = BloppsSuperficie,
    onSurface = BloppsTextoPrincipal,
    surfaceVariant = BloppsSuperficieAlta,
    onSurfaceVariant = BloppsTextoSecundario,
    outline = BloppsBorde,
    outlineVariant = BloppsBorde,
    error = BloppsError,
    onError = Color.White
)

@Composable
fun BloppsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaBlopps,
        typography = Typography,
        content = content
    )
}