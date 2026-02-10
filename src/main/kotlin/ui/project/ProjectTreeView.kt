package non.era.aisdme.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import non.era.aisdme.LocalAppController
import non.era.aisdme.Log
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.contexmenu.ProjectFileContextMenu
import non.era.aisdme.ui.editor.Panels

@Composable
fun ProjectTreeView(
    modifier: Modifier = Modifier
) {
    val app = LocalAppController.current
    LazyColumn(modifier = modifier.fillMaxHeight()) {
        items(app.getProjectState().getNodes()) { node ->
            ProjectNodeView(node)
        }
    }
}

@Composable
fun ProjectNodeView(
    node: ProjectNode
) {
    when (node) {
        is DirNode -> DirNodeView(node)
        is FileNode -> FileNodeView(node)
        is LinkNode -> LinkNodeView(node)
    }
}

//todo контекстное
@Composable
fun DirNodeView(
    node: DirNode
) {
    val app = LocalAppController.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = 6.dp)
                .then(
                    if(node.isSelect)
                        Modifier.background(LocalAisDmeColors.current.selectedRow)
                    else Modifier
                )
                .padding(start = (node.depth * 16).dp)
                .clickable {
                    node.expanded.value = !node.expanded.value
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            if (awaitPointerEvent().type == PointerEventType.Press) app.reselectElementTree(node)
                        }
                    }
                }
        ) {
            Text(color = if(node.isCutted) LocalAisDmeColors.current.cutted else Color.Unspecified,
                text = if (node.expanded.value) "📂 ${node.file.name}" else "📁 ${node.file.name}"
            )
        }


        if (node.expanded.value) {
            node.children.forEach { ch->
                ProjectNodeView(ch)
            }
        }
    }
}

@Composable
fun FileNodeView(node: FileNode) {
    val app = LocalAppController.current
    ProjectFileContextMenu(node) {
        Row(
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = 6.dp)
                .then(
                    if(node.isSelect)
                        Modifier.background(LocalAisDmeColors.current.selectedRow)
                    else Modifier
                )
                .padding(start = (node.depth * 16).dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            app.openFileInEditor(node.file, Panels.LEFT)
                        }
                    )
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            if (awaitPointerEvent().type == PointerEventType.Press) app.reselectElementTree(node)
                        }
                    }
                }
        ) {
            Text(color = if(node.isCutted) LocalAisDmeColors.current.cutted else Color.Unspecified,
                text = "📄 ${node.file.name}")
        }
    }
}

//todo контекстное
@Composable
fun LinkNodeView(node: LinkNode) {
    val app = LocalAppController.current
    Row(
        modifier = Modifier
            .fillMaxWidth().padding(horizontal = 6.dp)
            .then(
                if(node.isSelect)
                    Modifier.background(LocalAisDmeColors.current.selectedRow)
                else Modifier
            )
            .padding(start = (node.depth * 16).dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        if (awaitPointerEvent().type == PointerEventType.Press) app.reselectElementTree(node)
                    }
                }
            }
    ) {
        Text(color = if(node.isCutted) LocalAisDmeColors.current.cutted else Color.Unspecified,
            text = "L: ${node.file.name}")
    }
}