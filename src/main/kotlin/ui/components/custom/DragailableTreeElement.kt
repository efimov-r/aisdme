package non.era.aisdme.ui.components.custom

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import non.era.aisdme.di.AppController
import non.era.aisdme.ui.components.dialogs.elements.UiDialog
import non.era.aisdme.ui.project.data.ProjectNode

@Composable
fun DragailableTreeElement(app: AppController,
                           node: ProjectNode,
                           content: @Composable BoxScope.() -> Unit) {
    val moveController = app.moveController
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
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()

                    val anyPressed = event.changes.any { it.pressed }

                    if (!anyPressed) {
                        moveController.forceFinish()
                    }
                }
            }
        }
        .pointerInput(node) {
            detectDragGestures(
                onDragStart = { offset ->
                    moveController.onDragStart(offset, app.projectController.treeState.getSelectedNodes())
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
                        app.dialogController.requestDialog(UiDialog.ShowMoveDialog(src, f))
                    }
                },
                onDragCancel = {
                    moveController.cancelDrag()
                }
            )
        },
        content = content)
}