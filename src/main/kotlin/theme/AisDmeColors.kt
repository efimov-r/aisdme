package non.era.aisdme.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AisDmeColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurface: Color,
    val surfaceBorder: Color,
    val custom: Color,
    val selectedRow: Color,
    val cutted: Color,
    val transparent: Color,
    val error: Color,
)

val LightAisDmeColors = AisDmeColors(
    primary = Color(0xFF3574F0),
    onPrimary = Color(0xFFFFFFFF),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFD5D5D5),
    surface = Color(0xFFFFFFFF),
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceBorder = Color(0xFFb0b0b0),
    custom = Color.Red,
    selectedRow = Color(0xFFD5D5D5),
    cutted = Color(0xFF858585),
    transparent = Color(0x00000000),
    error = Color.Red,
)

val DarkAisDmeColors = AisDmeColors(
    primary = Color(0xFF3574F0),
    onPrimary = Color(0xFFFFFFFF),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212), // Основной фон
    surface = Color(0xFF1E1E1E),    // Поверхности (карточки, панели)
    onBackground = Color.White,     // Текст на фоне
    onSurface = Color.White,         // Текст на поверхностях
    surfaceBorder = Color(0xFFf0f0f0), //рамка карточек
    custom = Color.Green, //отладка
    selectedRow = Color(0xFFFFFFFF),
    cutted = Color(0xFF858585),
    transparent = Color(0x00000000),
    error = Color.Red,
    )

//!!!! Пример
//LocalAisDmeColors.current.custom