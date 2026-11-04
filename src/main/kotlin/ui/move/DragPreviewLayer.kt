package non.era.aisdme.ui.move

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.ui.components.over.NodeIcon

@Composable
fun DragPreviewLayer() {
    val moveController = LocalAppController.current.moveController
    val pos = moveController.getDragPosition() ?: return

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {} // чтобы не ловить события
    ) {
        Box(
            Modifier
                .offset {
                    IntOffset(
                        pos.x.toInt() + 12,
                        pos.y.toInt() + 12
                    )
                }
        ) {
            NodeIcon(Modifier, moveController.getNodesDragable()?.lastOrNull())
        }
    }
}