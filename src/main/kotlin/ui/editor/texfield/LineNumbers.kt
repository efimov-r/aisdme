package non.era.aisdme.ui.editor.texfield

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.theme.LocalAisDmeColors
import kotlin.math.max

@Composable
fun LineNumbers(
    layoutResult: TextLayoutResult?,
    scrollState: ScrollState,
    edgePadding: Dp
) {
    val colors = LocalAisDmeColors.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val edgePaddingPx = with(density) { edgePadding.toPx() }

    Canvas(
        Modifier
            .width(60.dp)
            .fillMaxHeight()
            .clipToBounds()
    ) {
        val layout = layoutResult ?: return@Canvas

        val scrollY = scrollState.value.toFloat()

        var line = layout.getLineForVerticalPosition(scrollY)
        line = max((line-2), 0)
        var y = layout.getLineTop(line) - scrollY + edgePaddingPx

        while (line < layout.lineCount && y < size.height) {
            drawText(
                textMeasurer = textMeasurer,
                text = (line + 1).toString(),
                topLeft = Offset(8f, y),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = colors.surfaceBorder
                )
            )

            line++
            if (line < layout.lineCount) {
                y = layout.getLineTop(line) - scrollY + edgePaddingPx
            }
        }
    }
}