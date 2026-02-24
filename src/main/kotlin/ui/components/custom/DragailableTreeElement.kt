package non.era.aisdme.ui.components.custom

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import non.era.aisdme.AppController
import non.era.aisdme.ui.components.dialogs.UiDialog
import non.era.aisdme.ui.project.data.ProjectNode

@Composable
fun DragailableTreeElement(app: AppController,
                           node: ProjectNode,
                           content: @Composable BoxScope.() -> Unit) {
    val moveController = app.getMoveController()
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    DisposableEffect(node) {
        onDispose {
            moveController.disposeElement(node)
        }
    }

    Box(modifier = Modifier
        .onGloballyPositioned {
            coords = it
            moveController.setNodeBounds(node, it.boundsInWindow())
        }
        .pointerInput(node) {
            detectDragGestures(
                onDragStart = { offset ->
                    moveController.onDragStart(offset, app.getSelectedNodes())
                },
                onDrag = { change, _ ->
                    change.consume()

                    coords?.let { layoutCoords ->
                        val windowPos = layoutCoords.localToWindow(change.position)
                        moveController.setDragPosition(windowPos)
                    }
                },
                onDragEnd = {
                    moveController.finishDrop{ src, f->
                        app.requestDialog(UiDialog.ShowMoveDialog(src, f))
                    }
                },
                onDragCancel = {
                    moveController.cancelDrag()
                }
            )
        },
        content = content)
}