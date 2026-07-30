package net.koalastuff.music.core.designsystem

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

private val LightColors = lightColorScheme(
    primary = Color(0xFF3E6B48),
    onPrimary = Color.White,
    secondary = Color(0xFF526451),
    surface = Color(0xFFF8FBF5),
    surfaceVariant = Color(0xFFE0E8DD)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA4D0A9),
    onPrimary = Color(0xFF0D381B),
    secondary = Color(0xFFBACCB8),
    surface = Color(0xFF111411),
    surfaceVariant = Color(0xFF414941)
)

@Composable
fun KoalaMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= 31 && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && Build.VERSION.SDK_INT >= 31 -> dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
