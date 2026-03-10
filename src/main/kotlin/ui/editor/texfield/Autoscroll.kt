package non.era.aisdme.ui.editor.texfield

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import non.era.aisdme.ui.components.custom.HorizontalScrollContent
import non.era.aisdme.ui.components.custom.ScrollableContent

@Composable
fun horizontalAutoscroll(
    layoutResult: TextLayoutResult?,
    viewportWidthPx: Int,

    textFieldValue: TextFieldValue,
    content: @Composable (scrollState: ScrollState) -> Unit
){
    val leftCursorPadding = 40.dp
    val rightCursorPadding = 120.dp

    HorizontalScrollContent { horizontalScrollState ->
        val density = LocalDensity.current
        LaunchedEffect(
            textFieldValue.selection,
            layoutResult,
            viewportWidthPx
        ) {
            val layout = layoutResult ?: return@LaunchedEffect

            val cursorOffset = textFieldValue.selection.start

            val cursorRect = try {
                layout.getCursorRect(cursorOffset)
            } catch (_: IllegalArgumentException) {
                return@LaunchedEffect
            }

            val leftPaddingPx = with(density) { leftCursorPadding.toPx() }
            val rightPaddingPx = with(density) { rightCursorPadding.toPx() }

            val visibleLeft = horizontalScrollState.value
            val visibleRight = horizontalScrollState.value + viewportWidthPx

            when {
                cursorRect.left < visibleLeft + leftPaddingPx -> {
                    val target = (cursorRect.left - leftPaddingPx).toInt().coerceAtLeast(0)
                    horizontalScrollState.animateScrollTo(target)
                }

                cursorRect.right > visibleRight - rightPaddingPx -> {
                    val target = (cursorRect.right - viewportWidthPx + rightPaddingPx)
                        .toInt()
                        .coerceAtLeast(0)
                    horizontalScrollState.animateScrollTo(target)
                }
            }
        }

        content(horizontalScrollState)
    }
}

@Composable
fun VerticalAutoscroll(
    layoutResult: TextLayoutResult?,
    viewportHeightPx: Int,
    textFieldValue: TextFieldValue,
    startEditorSpace: Dp,
    content: @Composable (scrollState: ScrollState) -> Unit
) {
    val endEditorSpaceAutoscroll = 20.dp

    ScrollableContent{ verticalScrollState->
        val density = LocalDensity.current
        LaunchedEffect(textFieldValue.selection, layoutResult, viewportHeightPx) {
            val layout = layoutResult ?: return@LaunchedEffect
            if (viewportHeightPx == 0) return@LaunchedEffect

            val cursorOffset = textFieldValue.selection.start
            val line = layout.getLineForOffset(cursorOffset)

            val lineTop = layout.getLineTop(line)
            val lineBottom = layout.getLineBottom(line)

            val topPaddingPx = with(density) { startEditorSpace.toPx() }
            val bottomPaddingPx = with(density) { endEditorSpaceAutoscroll.toPx() }

            val visibleTop = verticalScrollState.value
            val visibleBottom = verticalScrollState.value + viewportHeightPx

            when {
                lineTop < visibleTop + topPaddingPx -> { // Курсор слишком близко к верхнему краю
                    val targetScroll = (lineTop - topPaddingPx).toInt().coerceAtLeast(0)
                    verticalScrollState.animateScrollTo(targetScroll)
                }

                lineBottom > visibleBottom - bottomPaddingPx -> { // Курсор слишком близко к нижнему краю
                    val targetScroll = (lineBottom - viewportHeightPx + bottomPaddingPx).toInt()
                        .coerceAtLeast(0)
                    verticalScrollState.animateScrollTo(targetScroll)
                }
            }
        }

        content(verticalScrollState)
    }
}