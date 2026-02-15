package non.era.aisdme.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import non.era.aisdme.LocalAppController
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.contexmenu.ProjectTreeContextMenu
import non.era.aisdme.ui.components.custom.DragailableTreeElement
import non.era.aisdme.ui.components.custom.ProjectNodeClick
import non.era.aisdme.ui.components.dialogs.DeleteSelectedNodeDialog

@Composable
fun ProjectTreeView(modifier: Modifier = Modifier) {
    val app = LocalAppController.current

    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }

    var showDelDialog by remember { mutableStateOf(false) }
    if (showDelDialog) {
        DeleteSelectedNodeDialog(app) { showDelDialog = false }
    }

    ProjectTreeContextMenu {
        Box(modifier = modifier
            .fillMaxHeight().padding(horizontal = 6.dp)
            .focusRequester(focusRequester)
            .onFocusChanged {
                hasFocus = it.isFocused
            }
            .focusable()
            .onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                treeKeyFocusWork(it, app) {
                    showDelDialog = true
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        if (awaitPointerEvent().type == PointerEventType.Press) {
                            focusRequester.requestFocus()
                        }
                    }
                }
            }) {
            LazyColumn(modifier = modifier
                .fillMaxHeight()) {
                items(app.getNodes()) { node ->
                    ProjectNodeView(node, hasFocus)
                }
            }
        }
    }
}

@Composable
private fun ProjectNodeView(
    node: ProjectNode,
    hasFocus: Boolean,
) {
    if(node is DirNode) {
        Column {
            ProjectTreeNode(
                hasFocus = hasFocus,
                node = node,
            )

            if (node.expanded.value) {
                node.children.forEach { ch->
                    ProjectNodeView(ch, hasFocus)
                }
            }
        }
    } else {
        ProjectTreeNode(
            hasFocus = hasFocus,
            node = node,
        )
    }
}