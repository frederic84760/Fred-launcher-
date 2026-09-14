package fr.fred.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FredBlack = Color(0xFF000000)
val FredBlue = Color(0xFF2F80FF)
val FredBlueSoft = Color(0xFF8DB8FF)
val FredWhite = Color(0xFFF4F7FB)
val FredMuted = Color(0xFF8B93A3)
val FredSurface = Color(0xFF0A0D12)
val FredBorder = Color(0xFF1D2735)

private val FredColors = darkColorScheme(
    primary = FredBlue,
    onPrimary = FredWhite,
    background = FredBlack,
    onBackground = FredWhite,
    surface = FredSurface,
    onSurface = FredWhite,
    outline = FredBorder,
)

@Composable
fun FredLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FredColors,
        content = content,
    )
}
