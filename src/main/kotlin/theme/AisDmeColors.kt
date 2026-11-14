package non.era.aisdme.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AisDmeColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color, //меню, диалоги, карточки сообщений: над панелями
    val onBackground: Color,
    val onSurface: Color,
    val surfaceBorder: Color,
    val cutted: Color,
    val transparent: Color,
    val error: Color,
    val selectAndFocus: Color,
    val selectAndNotFocus: Color,
    val tabActive: Color, //активная вкладка в половине с фокусом
    val dropTargetElement: Color,
    val menuSelected: Color,
    val textSecondary: Color,
    val menuDivider: Color,
    val hoverButton: Color, //полупрозрачный: кнопки на разном фоне
    val lineNumber: Color, //номера строк в редакторе
    val scrim: Color, //затемнение окна за диалогом
    val statusLoad: Color, //статусы записи в панели под редактором
    val statusOk: Color,
    val statusWarn: Color,
    val statusAlert: Color,
    val statusError: Color,
    val onStatus: Color,
)

// Палитра по мотивам Manjaro, как в проекте Seryve: бирюзово-зелёный акцент на сине-серых («slate») поверхностях
val LightAisDmeColors = AisDmeColors(
    primary = Color(0xFF12876A),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFEEF1F3),
    surface = Color(0xFFFAFBFC),
    surfaceRaised = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C2227),
    onSurface = Color(0xFF1C2227),
    surfaceBorder = Color(0xFFCBD3D9),
    cutted = Color(0xFF7B8994),
    transparent = Color(0x00000000),
    error = Color(0xFFC62828),
    selectAndFocus = Color(0xFFC8EEE3),
    selectAndNotFocus = Color(0xFFDFE5E9),
    tabActive = Color(0xFFA1E4D1),
    dropTargetElement = Color(0xFFE4F7F1),
    menuSelected = Color(0xFFC8EEE3),
    textSecondary = Color(0xFF4D5A64),
    menuDivider = Color(0xFFDFE5E9),
    hoverButton = Color(0x1A12876A),
    lineNumber = Color(0xFFA3AFB8),
    scrim = Color(0x33000000),
    statusLoad = Color(0xFFD8CF3F),
    statusOk = Color(0xFF4FD16B),
    statusWarn = Color(0xFFFFC908),
    statusAlert = Color(0xFFF38100),
    statusError = Color(0xFFE5534B),
    onStatus = Color(0xFF1C2227),
)

val DarkAisDmeColors = AisDmeColors(
    primary = Color(0xFF28B48C),
    onPrimary = Color(0xFF00261C),
    background = Color(0xFF1C2227), // Основной фон
    surface = Color(0xFF263036),    // Поверхности (карточки, панели)
    surfaceRaised = Color(0xFF2D3840),
    onBackground = Color(0xFFE1E6EA), // Текст на фоне
    onSurface = Color(0xFFE1E6EA),    // Текст на поверхностях
    surfaceBorder = Color(0xFF3A4752), //рамка карточек
    cutted = Color(0xFF6B7C88),
    transparent = Color(0x00000000),
    error = Color(0xFFF06B6B),
    selectAndFocus = Color(0xFF1C4A44),
    selectAndNotFocus = Color(0xFF354450),
    tabActive = Color(0xFF1C4A44),
    dropTargetElement = Color(0xFF1C3635),
    menuSelected = Color(0xFF1C4A44),
    textSecondary = Color(0xFFA8B6C0),
    menuDivider = Color(0xFF3A4752),
    hoverButton = Color(0x2628B48C),
    lineNumber = Color(0xFF52616C),
    scrim = Color(0x66000000),
    statusLoad = Color(0xFFB8A83A),
    statusOk = Color(0xFF2E9E57),
    statusWarn = Color(0xFFD9A441),
    statusAlert = Color(0xFFD9772B),
    statusError = Color(0xFFD14B4B),
    onStatus = Color(0xFF161B1F),
)
