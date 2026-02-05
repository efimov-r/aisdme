package non.era.aisdme.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AisDmeColors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurface: Color,
    val custom: Color,
)

val LightAisDmeColors = AisDmeColors(
    primary = Color(0xFF6200EE),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFD5D5D5),
    surface = Color(0xFFFFFFFF),
    onBackground = Color.Black,
    onSurface = Color.Black,
    custom = Color.Red
)

val DarkAisDmeColors = AisDmeColors(
    primary = Color(0xFFBB86FC),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212), // Основной фон
    surface = Color(0xFF1E1E1E),    // Поверхности (карточки, панели)
    onBackground = Color.White,     // Текст на фоне
    onSurface = Color.White,         // Текст на поверхностях
    custom = Color.Green
)

//!!!! Пример
//LocalAisDmeColors.current.custom