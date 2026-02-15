package non.era.aisdme.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
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
import non.era.aisdme.ui.components.dialogs.DeleteSelectedNodeDialog
import non.era.aisdme.ui.editor.Panels

@Composable
fun ProjectTreeView(modifier: Modifier = Modifier) {
    val app = LocalAppController.current

    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        DeleteSelectedNodeDialog(app) { showDialog = false }
    }

    ProjectTreeContextMenu {
        Box(modifier = modifier
            .fillMaxHeight()
            .focusRequester(focusRequester)
            .onFocusChanged {
                hasFocus = it.isFocused
            }
            .focusable()
            .onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                treeKeyFocusWork(it, app) {
                    showDialog = true
                }
            }) {
            LazyColumn(modifier = modifier
                .fillMaxHeight()) {
                items(app.getNodes()) { node ->
                    ProjectNodeView(node, focusRequester, hasFocus)
                }
            }
        }
    }
}

@Composable
private fun ProjectNodeView(
    node: ProjectNode,
    focusRequester: FocusRequester,
    hasFocus: Boolean,
) {
    val app = LocalAppController.current

    DisposableEffect(node) {
        onDispose {
            app.getNodeBounds().remove(node)
        }
    }

    Box {
        when (node) {
            is DirNode -> DirNodeView(node, focusRequester, hasFocus)
            is FileNode -> FileNodeView(node, focusRequester, hasFocus)
            is LinkNode -> LinkNodeView(node, focusRequester, hasFocus)
        }
    }
}

@Composable
private fun DirNodeView(node: DirNode, focusRequester: FocusRequester, hasFocus: Boolean) =
    Column {
            NodeView(
                focusRequester = focusRequester,
                hasFocus = hasFocus,
                text = if (node.expanded.value) "📂 ${node.getFileName()}" else "📁 ${node.getFileName()}",
                node = node,
                doubleClick = {
                    node.openOrClose()
                }
            )

        if (node.expanded.value) {
            node.children.forEach { ch->
                ProjectNodeView(ch, focusRequester, hasFocus)
            }
        }
    }

@Composable
private fun FileNodeView(node: FileNode, focusRequester: FocusRequester, hasFocus: Boolean,) {
    val app = LocalAppController.current
        NodeView(
            focusRequester = focusRequester,
            hasFocus = hasFocus,
            text = "📄 ${node.getFileName()}",
            node = node,
            doubleClick = { app.doubleFileClick(node, Panels.LEFT) }
            )
}

@Composable
private fun LinkNodeView(node: LinkNode, focusRequester: FocusRequester, hasFocus: Boolean) =
        NodeView(
            focusRequester = focusRequester,
            hasFocus = hasFocus,
            text = "L: ${node.getFileName()}",
            node = node,
            )

@Composable
private fun NodeView(
             focusRequester: FocusRequester,
             hasFocus: Boolean,
             text: String,
             node: ProjectNode,
             doubleClick: (Offset) -> Unit = {}) {
    val app = LocalAppController.current
    val isSelected = node.isSelected()

    val isDropTarget = if(app.getDragState().draggingNode) {
        app.getNodeBounds()[node]?.contains(app.getDragState().dragPosition) == true
    } else {
        false
    }

    DragailableTreeElement(app, node) {
        Row(
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = 6.dp)
                .background(
                    when {
                        isSelected && hasFocus ->
                            LocalAisDmeColors.current.primary.copy(alpha = 0.5f)

                        isSelected && !hasFocus ->
                            LocalAisDmeColors.current.selectedRow.copy(alpha = 0.5f)

                        isDropTarget ->
                            LocalAisDmeColors.current.primary.copy(alpha = 0.2f)

                        else -> Color.Transparent
                    }
                )
                .padding(start = (node.depth * 16).dp)
                .pointerInput(node) {
                    detectTapGestures(
                        onDoubleTap = doubleClick
                    )
                }
                .pointerInput(node) {
                    awaitPointerEventScope {
                        while (true) { //todo выбор элемента при отпускании или при нажатии в зависимости от количества
                            if (awaitPointerEvent().type == PointerEventType.Press) {
                                focusRequester.requestFocus()
                                app.reselectNode(node)
                            }
                        }
                    }
                }
        ) {
            Text(color = if(node.isCutted) LocalAisDmeColors.current.cutted else Color.Unspecified,
                text = text)
        }
    }
}