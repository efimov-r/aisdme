package non.era.aisdme.ui.components.custom

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.awt.Cursor

//todo сохранение в настройки!!!
@Composable
fun ResizableProporcialRow(
    modifier: Modifier,
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit
) {
    val density = LocalDensity.current

    var rowWidthPx by remember { mutableStateOf(0f) }
    var leftRatio by remember { mutableStateOf(0.5f) } // 50 / 50 по умолчанию

    val minLeftPx = with(density) { 10.dp.toPx() }
    val minRightPx = with(density) { 10.dp.toPx() }

    val minLeftRatio = if (rowWidthPx > 0) minLeftPx / rowWidthPx else 0f
    val maxLeftRatio = if (rowWidthPx > 0) 1f - (minRightPx / rowWidthPx) else 1f

    Row(
        modifier.onGloballyPositioned {
            rowWidthPx = it.size.width.toFloat()
        }
    ) {
        val leftWidthPx = rowWidthPx * leftRatio

        Box(
            modifier = Modifier
                .width(with(density) { leftWidthPx.toDp() })
                .fillMaxHeight()
        ) {
            leftContent()
        }

        // Divider / drag handle
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .cursorForHorizontalPropResize()
                .pointerInput(rowWidthPx) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()

                        if (rowWidthPx > 0f) {
                            val deltaRatio = dragAmount.x / rowWidthPx
                            leftRatio = (leftRatio + deltaRatio)
                                .coerceIn(minLeftRatio, maxLeftRatio)
                        }
                    }
                }
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            rightContent()
        }
    }
}

fun Modifier.cursorForHorizontalPropResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))