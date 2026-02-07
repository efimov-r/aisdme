package non.era.aisdme.ui.project

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import non.era.aisdme.FileFMOpener
import non.era.aisdme.LocalAppController
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
    }
}

@Composable
fun DirNodeView(
    node: DirNode
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (node.depth * 16).dp)
                .clickable {
                    node.expanded.value = !node.expanded.value
                }
        ) {
            Text(
                text = if (node.expanded.value) "📂 ${node.file.name}"
                else "📁 ${node.file.name}"
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
fun FileNodeView(
    node: FileNode
) {
    var lastClickTime by remember { mutableStateOf(0L) }
    val app = LocalAppController.current

    ContextMenuArea(
        items = {
            listOf(
                ContextMenuItem("Open in Left", onClick = {app.openFile(node.file, Panels.LEFT) }),
                ContextMenuItem("Open in Right", onClick = {app.openFile(node.file, Panels.RIGHT)}),
                ContextMenuItem("Open in Dolphin", onClick = { FileFMOpener.openFileInFileManager(node.file)}),
            )
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (node.depth * 16).dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime < 350) {
                                app.openFile(node.file, Panels.LEFT)
                            }
                            lastClickTime = now
                        }
                    )
                }
        ) {
            Text("📄 ${node.file.name}")
        }
    }
}