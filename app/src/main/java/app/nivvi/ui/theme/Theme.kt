package app.nivvi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

val MidnightBlue = Color(0xFF0B1426)
val DeepBlue = Color(0xFF152238)
val CardBlue = Color(0xFF1C2C44)
val Mint = Color(0xFF6EE7B7)
val Coral = Color(0xFFFF6B6B)
val Lavender = Color(0xFFC4B5FD)
val SoftWhite = Color(0xFFF1F5F9)
val Muted = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = MidnightBlue,
    secondary = Lavender,
    onSecondary = MidnightBlue,
    tertiary = Coral,
    background = MidnightBlue,
    surface = DeepBlue,
    surfaceVariant = CardBlue,
    onBackground = SoftWhite,
    onSurface = SoftWhite,
    onSurfaceVariant = Muted,
    error = Coral
)

private val NivviTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 96.sp,
        letterSpacing = (-1.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun NivviTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = NivviTypography,
        content = content
    )
}
