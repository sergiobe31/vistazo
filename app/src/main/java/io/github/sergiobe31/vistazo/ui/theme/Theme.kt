package io.github.sergiobe31.vistazo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.sergiobe31.vistazo.R

// JetBrains Mono empaquetada (OFL): la app no tiene permiso de red.
private val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
)

// Paleta fija (identidad v2): papel crema + ámbar. Sin color dinámico.
private val LightColors = lightColorScheme(
    background = Color(0xFFFAF3D9), // papel viejo suave
    surface = Color(0xFFFAF3D9),
    surfaceVariant = Color(0xFFF1E7C6),
    onSurface = Color(0xFF3F3222), // tinta marrón
    onSurfaceVariant = Color(0xFF7A6A4F),
    primary = Color(0xFFB45309), // ámbar oscuro
    onPrimary = Color(0xFFFAF3D9),
    secondary = Color(0xFF7A6A4F),
    outline = Color(0xFFD9CCA4),
)

private val DarkColors = darkColorScheme(
    background = Color(0xFF241D12), // marrón muy oscuro
    surface = Color(0xFF241D12),
    surfaceVariant = Color(0xFF2F2718),
    onSurface = Color(0xFFEDE3CB),
    onSurfaceVariant = Color(0xFFA89878),
    primary = Color(0xFFE8A33D), // ámbar claro
    onPrimary = Color(0xFF2A1E08),
    secondary = Color(0xFFA89878),
    outline = Color(0xFF4A3F2A),
)

private val VistazoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 88.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 56.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 30.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal, fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal, fontSize = 14.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal, fontSize = 12.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 11.sp,
        letterSpacing = 0.6.sp,
    ),
)

@Composable
fun VistazoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = VistazoTypography,
        content = content,
    )
}
