package non.era.aisdme.ui.components.custom

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
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
import non.era.aisdme.ui.project.ProjectNode
import kotlin.collections.set

@Composable
fun DragailableElement(app: AppController,
                       node: ProjectNode,
                       content: @Composable BoxScope.() -> Unit) {
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    Box(modifier = Modifier
        .onGloballyPositioned {
            coords = it
            app.getNodeBounds()[node] = it.boundsInWindow()
        }
        .pointerInput(node) {
            detectDragGestures(
                onDragStart = { offset ->
                    app.getDragState().draggingNode = node
                    app.getDragState().dragPosition = offset
                },
                onDrag = { change, _ ->
                    change.consume()

                    coords?.let { layoutCoords ->
                        val windowPos = layoutCoords.localToWindow(change.position)
                        app.getDragState().dragPosition = windowPos
                    }
                },
                onDragEnd = {
                    app.finishDrop()
                },
                onDragCancel = {
                    app.getDragState().draggingNode = null
                }
            )
        },
        content = content)
}