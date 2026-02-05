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
fun ResizableRow(
    modifier: Modifier,
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var leftWidthPx by remember { mutableStateOf(250f) }
    var widthRow by remember { mutableStateOf(0f) }

    val minLeftPx = with(density) { 10.dp.toPx() }
    val minRightPx = with(density) { 10.dp.toPx() }

    // Эффект для корректировки leftWidthPx при изменении widthRow
    LaunchedEffect(widthRow) {
        if (widthRow > 0) {
            // Проверяем, что leftWidthPx не выходит за новые границы
            val maxLeftPx = widthRow - minRightPx
            if (leftWidthPx > maxLeftPx) {
                leftWidthPx = maxLeftPx.coerceAtLeast(minLeftPx)
            }
        }
    }

    Row(
        modifier
            .onGloballyPositioned { layoutCoordinates ->
                widthRow = layoutCoordinates.size.width.toFloat()
            })
    {
        Box(
            modifier = Modifier
                .width(with(density) { leftWidthPx.toDp() })
                .fillMaxHeight()
        ) {
            leftContent()
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp) // делаем шире для удобства
                .cursorForHorizontalResize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()

                        leftWidthPx = (leftWidthPx + dragAmount.x)
                            .coerceIn(minLeftPx, (widthRow - minRightPx))
                    }
                }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            rightContent()
        }
    }
}

fun Modifier.cursorForHorizontalResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))