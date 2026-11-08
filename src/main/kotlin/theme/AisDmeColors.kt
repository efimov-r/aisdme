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
    val surfaceRaised: Color, //меню, диалоги, карточки сообщений: над панелями
    val onBackground: Color,
    val onSurface: Color,
    val surfaceBorder: Color,
    val custom: Color,
    val selectedRow: Color,
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

val LightAisDmeColors = AisDmeColors(
    primary = Color(0xFF35F074),
    onPrimary = Color(0xFF0B2615),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFE9EBEE),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E2126),
    onSurface = Color(0xFF1E2126),
    surfaceBorder = Color(0xFFD0D3D8),
    custom = Color.Red,
    selectedRow = Color(0xFFD5D5D5),
    cutted = Color(0xFF858585),
    transparent = Color(0x00000000),
    error = Color(0xFFD32F2F),
    selectAndFocus = Color(0xFFCDEFD9),
    selectAndNotFocus = Color(0xFFECEEF1),
    tabActive = Color(0xFFA6EBC1),
    dropTargetElement = Color(0xFFE1F6E8),
    menuSelected = Color(0xFFCDEFD9),
    textSecondary = Color(0xFF6B7078),
    menuDivider = Color(0xFFE4E6EA),
    hoverButton = Color(0x1A35F074),
    lineNumber = Color(0xFFB4B8BF),
    scrim = Color(0x33000000),
    statusLoad = Color(0xFFD8CF3F),
    statusOk = Color(0xFF4FD16B),
    statusWarn = Color(0xFFFFC908),
    statusAlert = Color(0xFFF38100),
    statusError = Color(0xFFE5534B),
    onStatus = Color(0xFF1E2126),
)

val DarkAisDmeColors = AisDmeColors(
    primary = Color(0xFF2EC866),
    onPrimary = Color(0xFF0B2615),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF131416), // Основной фон
    surface = Color(0xFF1E2024),    // Поверхности (карточки, панели)
    surfaceRaised = Color(0xFF26282D),
    onBackground = Color(0xFFDFE1E5), // Текст на фоне
    onSurface = Color(0xFFDFE1E5),    // Текст на поверхностях
    surfaceBorder = Color(0xFF2E3137), //рамка карточек
    custom = Color.Green, //отладка
    selectedRow = Color(0xFF2B2E33),
    cutted = Color(0xFF6B7078),
    transparent = Color(0x00000000),
    error = Color(0xFFF06B6B),
    selectAndFocus = Color(0xFF1F4D33),
    selectAndNotFocus = Color(0xFF2B2E33),
    tabActive = Color(0xFF1F4D33),
    dropTargetElement = Color(0xFF16291E),
    menuSelected = Color(0xFF1F4D33),
    textSecondary = Color(0xFF8B9098),
    menuDivider = Color(0xFF2E3137),
    hoverButton = Color(0x262EC866),
    lineNumber = Color(0xFF5B6068),
    scrim = Color(0x66000000),
    statusLoad = Color(0xFFB8A83A),
    statusOk = Color(0xFF2E9E57),
    statusWarn = Color(0xFFD9A441),
    statusAlert = Color(0xFFD9772B),
    statusError = Color(0xFFD14B4B),
    onStatus = Color(0xFF14161A),
)

//!!!! Пример
//LocalAisDmeColors.current.custom
