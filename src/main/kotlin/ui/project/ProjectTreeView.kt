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
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import non.era.aisdme.LocalAppController
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.contexmenu.ProjectDirContextMenu
import non.era.aisdme.ui.components.contexmenu.ProjectFileContextMenu
import non.era.aisdme.ui.components.contexmenu.ProjectSymlinkContextMenu
import non.era.aisdme.ui.components.dialogs.ConfirmDialog
import non.era.aisdme.ui.components.dialogs.DeleteNodeDialog
import non.era.aisdme.ui.editor.Panels

@Composable
fun ProjectTreeView(
    modifier: Modifier = Modifier
) {
    val app = LocalAppController.current

    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) app.getProjectState().getSelectedNode()?.let {
        DeleteNodeDialog(app, it) { showDialog = false }
    } ?: { showDialog = false }

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
            items(app.getProjectState().getNodes()) { node ->
                ProjectNodeView(node, focusRequester, hasFocus)
            }
        }
    }
}

@Composable
fun ProjectNodeView(
    node: ProjectNode,
    focusRequester: FocusRequester,
    hasFocus: Boolean,
) {
    when (node) {
        is DirNode -> DirNodeView(node, focusRequester, hasFocus)
        is FileNode -> FileNodeView(node, focusRequester, hasFocus)
        is LinkNode -> LinkNodeView(node, focusRequester, hasFocus)
    }
}

@Composable
fun DirNodeView(
    node: DirNode,
    focusRequester: FocusRequester,
    hasFocus: Boolean,
) {
    val app = LocalAppController.current
    val isSelected = app.getProjectState().getSelectedNode()?.file == node.file
    val isCutted = node.isCutted

    Column {
        ProjectDirContextMenu(node) {
            NodeView(
                isSelected = isSelected,
                isCutted = isCutted,
                focusRequester = focusRequester,
                hasFocus = hasFocus,
                text = if (node.expanded.value) "📂 ${node.file.name}" else "📁 ${node.file.name}",
                node = node,
                click = {
                    node.openOrClose()
                }
            )
        }

        if (node.expanded.value) {
            node.children.forEach { ch->
                ProjectNodeView(ch, focusRequester, hasFocus)
            }
        }
    }
}

@Composable
fun FileNodeView(node: FileNode,
                 focusRequester: FocusRequester,
                 hasFocus: Boolean,) {
    val app = LocalAppController.current
    val isSelected = app.getProjectState().getSelectedNode()?.file == node.file
    val isCutted = node.isCutted

    ProjectFileContextMenu(node) {
        NodeView(
            isSelected = isSelected,
            isCutted = isCutted,
            focusRequester = focusRequester,
            hasFocus = hasFocus,
            text = "📄 ${node.file.name}",
            node = node,
            doubleClick = { app.openFileInEditor(node.file, Panels.LEFT) }
            )
    }
}

@Composable
fun LinkNodeView(node: LinkNode,
                 focusRequester: FocusRequester,
                 hasFocus: Boolean,) {
    val app = LocalAppController.current
    val isSelected = app.getProjectState().getSelectedNode()?.file == node.file
    val isCutted = node.isCutted

    ProjectSymlinkContextMenu(node) {
        NodeView(
            isSelected = isSelected,
            isCutted = isCutted,
            focusRequester = focusRequester,
            hasFocus = hasFocus,
            text = "L: ${node.file.name}",
            node = node,
            )
    }
}

@Composable
fun NodeView(
             isSelected: Boolean,
             isCutted: Boolean,
             focusRequester: FocusRequester,
             hasFocus: Boolean,
             text: String,
             node: ProjectNode,
             click: () -> Unit = {},
             doubleClick: (Offset) -> Unit = {}) {
    val app = LocalAppController.current
    Row(
        modifier = Modifier
            .fillMaxWidth().padding(horizontal = 6.dp)
            .background(
                when {
                    isSelected && hasFocus ->
                        LocalAisDmeColors.current.primary.copy(alpha = 0.5f)

                    isSelected && !hasFocus ->
                        LocalAisDmeColors.current.selectedRow.copy(alpha = 0.5f)

                    else -> Color.Transparent
                }
            )
            .padding(start = (node.depth * 16).dp)
            .pointerInput(node) {
                detectTapGestures(
                    onDoubleTap = doubleClick,
                    onPress = { click() }
                )
            }
            .pointerInput(node) {
                awaitPointerEventScope {
                    while (true) {
                        if (awaitPointerEvent().type == PointerEventType.Press) {
                            focusRequester.requestFocus()
                            app.reselectNode(node)
                        }
                    }
                }
            }
    ) {
        Text(color = if(isCutted) LocalAisDmeColors.current.cutted else Color.Unspecified,
            text = text)
    }
}