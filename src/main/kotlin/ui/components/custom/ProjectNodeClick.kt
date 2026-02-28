package non.era.aisdme.ui.components.custom

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import non.era.aisdme.AppController
import non.era.aisdme.ui.project.data.ProjectNode
import non.era.aisdme.ui.project.TypeClickNode

@Composable
fun ProjectNodeClick(
    node: ProjectNode,
    app: AppController,
    content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .pointerInput(node) {
                detectTapGestures(
                    onDoubleTap = {
                        app.projectController.clickNode(node, TypeClickNode.DLMB)
                    }
                )
            }
            .pointerInput(node) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Press -> {
                                when {
                                    event.buttons.isPrimaryPressed -> {
                                        if(event.keyboardModifiers.isShiftPressed) {
                                            app.projectController.clickNode(node, TypeClickNode.SHIFT_LMB)
                                        } else if(event.keyboardModifiers.isCtrlPressed){
                                            app.projectController.clickNode(node, TypeClickNode.CTRL_LMB)
                                        } else {
                                            app.projectController.clickNode(node, TypeClickNode.LMB)
                                        }
                                    }

                                    event.buttons.isSecondaryPressed -> {
                                        app.projectController.clickNode(node, TypeClickNode.RMB)
                                    }
                                }
                            }

                            PointerEventType.Release -> {
                                when {
                                    event.buttons.isPrimaryPressed.not() -> {
                                        app.projectController.clickNode(node, TypeClickNode.DOWN)
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                }
            },
        content = content)
}