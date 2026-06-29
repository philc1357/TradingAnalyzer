package com.example.tradinganalyser.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// Dunkles Trading-Theme. Farben aus der Referenz (levin/static/style.css).
// ============================================================
val BgColor = Color(0xFF0E1117)
val CardColor = Color(0xFF161B22)
val BorderColor = Color(0xFF2A313C)
val TextColor = Color(0xFFE6EDF3)
val MutedColor = Color(0xFF8B949E)
val AccentColor = Color(0xFF2DD4A7)
val PosColor = Color(0xFF2DD4A7)
val NegColor = Color(0xFFF85149)

private val DarkColors = darkColorScheme(
    primary = AccentColor,
    onPrimary = Color(0xFF06241C),
    background = BgColor,
    onBackground = TextColor,
    surface = CardColor,
    onSurface = TextColor,
    surfaceVariant = CardColor,
    onSurfaceVariant = MutedColor,
    error = NegColor,
)

@Composable
fun TradingAnalyzerTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // App ist bewusst durchgehend dunkel gehalten.
    MaterialTheme(colorScheme = DarkColors, content = content)
}
