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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun Modifier.dialogBorder() = this.border(1.dp, LocalAisDmeColors.current.surfaceBorder, AisDmeShapes.medium)

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun aisDmeDialogProperties() = DialogProperties( scrimColor = LocalAisDmeColors.current.scrim )

@Composable
fun aisDmeButtonBorder() = BorderStroke(1.dp, LocalAisDmeColors.current.surfaceBorder)

private fun AisDmeColors.toMaterial(dark: Boolean) = //Material берёт цвета у нас: текст по умолчанию, поля ввода
    if (dark) darkColors(
        primary = primary,
        background = background,
        surface = surface,
        onPrimary = onPrimary,
        onBackground = onBackground,
        onSurface = onSurface,
        error = error,
    ) else lightColors(
        primary = primary,
        background = background,
        surface = surface,
        onPrimary = onPrimary,
        onBackground = onBackground,
        onSurface = onSurface,
        error = error,
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
            colors = colors.toMaterial(darkTheme),
            typography = MaterialTheme.typography,
            shapes = NoRoundedShapes,
            content = content
        )
    }
}