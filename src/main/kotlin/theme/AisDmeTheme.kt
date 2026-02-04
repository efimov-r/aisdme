package non.era.aisdme.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val isDarkTheme = false // Можно сделать переключатель
val colors = if (isDarkTheme) {
    darkColors(
        primary = Color(0xFFBB86FC),
        primaryVariant = Color(0xFF3700B3),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFF121212), // Основной фон
        surface = Color(0xFF1E1E1E),    // Поверхности (карточки, панели)
        onBackground = Color.White,     // Текст на фоне
        onSurface = Color.White         // Текст на поверхностях
    )
} else {
    lightColors(
        primary = Color(0xFF6200EE),
        primaryVariant = Color(0xFF3700B3),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF5F5F5),
        onBackground = Color.Black,
        onSurface = Color.Black
    )
}

@Composable
fun AisDmeTheme(content: @Composable () -> Unit) =
    MaterialTheme(
        colors,
        MaterialTheme.typography,
        MaterialTheme.shapes,
        content)