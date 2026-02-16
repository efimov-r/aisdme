package non.era.aisdme.ui.components.custom

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun ResizableColumn(
    modifier: Modifier,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var bottomHeightPx by remember { mutableStateOf(250f) }
    var heightColumn by remember { mutableStateOf(0f) }

    val minTopPx = with(density) { 10.dp.toPx() }
    val minBottomPx = with(density) { 10.dp.toPx() }

    // Эффект для корректировки leftWidthPx при изменении widthRow
    LaunchedEffect(heightColumn) {
        if (heightColumn > 0) {  // Проверяем, что leftWidthPx не выходит за новые границы
            val maxTopPx = heightColumn - minBottomPx
            if (bottomHeightPx > maxTopPx) {
                bottomHeightPx = maxTopPx.coerceAtLeast(minTopPx)
            }
        }
    }

    Column(modifier
            .onGloballyPositioned { layoutCoordinates ->
                heightColumn = layoutCoordinates.size.height.toFloat()
            })
    {
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
            content = { topContent() })

        Box(modifier = Modifier
                .fillMaxWidth()
                .height(6.dp) // делаем шире для удобства
                .cursorForVerticalResize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        bottomHeightPx = (bottomHeightPx - dragAmount.y)
                            .coerceIn(minBottomPx, (heightColumn-minTopPx))
                    }
                }
        )

        Box(modifier = Modifier.fillMaxWidth().height(with(density) { bottomHeightPx.toDp() }),
            content = { bottomContent() })
    }
}

fun Modifier.cursorForVerticalResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))