package non.era.aisdme.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun Modifier.dialogBorder() = this.border(1.dp, LocalAisDmeColors.current.surfaceBorder, AisDmeShapes.medium)

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun aisDmeDialogProperties() = DialogProperties( scrimColor = LocalAisDmeColors.current.alertDialogScreem )

@Composable
fun aisDmeButtonBorder() = BorderStroke(1.dp, LocalAisDmeColors.current.surfaceBorder)

val darkColors = darkColors(
        primary = Color(0xFFBB86FC),
        primaryVariant = Color(0xFF3700B3),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFF121212), // Основной фон
        surface = Color(0xFF1E1E1E),  // Поверхности (карточки, панели)
        onBackground = Color.White, // Текст на фоне
        onSurface = Color.White // Текст на поверхностях
    )

val lightColors = lightColors(
        primary = Color(0xFF6200EE),
        primaryVariant = Color(0xFF3700B3),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFFD5D5D5),
        surface = Color(0xFFFFFFFF),
        onBackground = Color.Black,
        onSurface = Color.Black
    )

val NoRoundedShapes = Shapes(
    small = RoundedCornerShape(0.dp), // маленькие элементы
    medium = RoundedCornerShape(0.dp), // средние элементы
    large = RoundedCornerShape(0.dp) // большие элементы
 )


val LocalAisDmeColors = staticCompositionLocalOf<AisDmeColors> {
    error("AisDmeColors not provided")
}

@Composable
fun AisDmeTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkAisDmeColors else LightAisDmeColors

    CompositionLocalProvider(
        LocalAisDmeColors provides colors
    ) {
        MaterialTheme(
            colors = if (darkTheme)  darkColors  else  lightColors ,
            typography = MaterialTheme.typography,
            shapes = NoRoundedShapes,
            content = content
        )
    }
}