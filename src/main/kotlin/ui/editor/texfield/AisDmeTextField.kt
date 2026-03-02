package non.era.aisdme.ui.editor.texfield

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import non.era.aisdme.ui.components.custom.ScrollableContent
import non.era.aisdme.ui.editor.ObservableOpenedFile
import kotlin.math.max

@Composable
fun AisDmeTextField(
    focusRequester: FocusRequester,
    onFocusChanged: (FocusState) -> Unit,
    file: ObservableOpenedFile,
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val startEditorSpace = 4.dp
    val endEditorSpace = 120.dp

    ScrollableContent { scrollState->
        Row(
            Modifier.fillMaxSize()
        ) {
            LineNumbers(
                layoutResult = layoutResult,
                scrollState = scrollState,
                edgePadding = startEditorSpace + 3.dp
            )

            Spacer(Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(LocalAisDmeColors.current.surfaceBorder)
            )

            Spacer(Modifier.width(2.dp))

            Column(
                Modifier
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ){
                Spacer(Modifier.height(startEditorSpace))
                BasicTextField(
                    value = file.textFieldValue,
                    readOnly = file.writeStatus.readOnly,
                    onValueChange = { newValue ->
                        file.updateTextFieldValue(newValue)
                    },
                    textStyle = TextStyle(
                        color = LocalAisDmeColors.current.onSurface,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            onFocusChanged(it)
                        },
                    onTextLayout = { layoutResult = it }
                )
                Spacer(Modifier.height(endEditorSpace))
            }
        }
    }
}

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