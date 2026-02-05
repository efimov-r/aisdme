package non.era.aisdme.ui.components.custom

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
fun ResizableColumn(
    modifier: Modifier,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var topHeightPx by remember { mutableStateOf(250f) }
    var heightRow by remember { mutableStateOf(0f) }

    val minTopPx = with(density) { 10.dp.toPx() }
    val minBottomPx = with(density) { 10.dp.toPx() }

    // Эффект для корректировки leftWidthPx при изменении widthRow
    LaunchedEffect(heightRow) {
        if (heightRow > 0) {
            // Проверяем, что leftWidthPx не выходит за новые границы
            val maxTopPx = heightRow - minBottomPx
            if (topHeightPx > maxTopPx) {
                topHeightPx = maxTopPx.coerceAtLeast(minTopPx)
            }
        }
    }

    Column(
        modifier
            .onGloballyPositioned { layoutCoordinates ->
                heightRow = layoutCoordinates.size.height.toFloat()
            })
    {
        Box(
            modifier = Modifier
                .height(with(density) { topHeightPx.toDp() })
                .fillMaxHeight()
        ) {
            topContent()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp) // делаем шире для удобства
                .cursorForVerticalResize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()

                        topHeightPx = (topHeightPx + dragAmount.y)
                            .coerceIn(minTopPx, (heightRow - minBottomPx))
                    }
                }
        ) {
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.Center)
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            bottomContent()
        }
    }
}

fun Modifier.cursorForVerticalResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))