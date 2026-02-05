package non.era.aisdme.ui.project

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
import java.io.File

@Composable
fun ProjectTreeView(
    controller: ProjectController,
    onFileOpen: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxHeight()) {
        items(controller.nodes) { node ->
            ProjectNodeView(node, onFileOpen)
        }
    }
}

@Composable
fun ProjectNodeView(
    node: ProjectNode,
    onFileOpen: (File) -> Unit
) {
    when (node) {
        is DirNode -> DirNodeView(node, onFileOpen)
        is FileNode -> FileNodeView(node, onFileOpen)
    }
}

@Composable
fun DirNodeView(
    node: DirNode,
    onFileOpen: (File) -> Unit
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
            node.children.forEach {
                ProjectNodeView(it, onFileOpen)
            }
        }
    }
}

@Composable
fun FileNodeView(
    node: FileNode,
    onFileOpen: (File) -> Unit
) {
    var lastClickTime by remember { mutableStateOf(0L) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (node.depth * 16).dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        val now = System.currentTimeMillis()
                        if (now - lastClickTime < 350) {
                            onFileOpen(node.file)
                        }
                        lastClickTime = now
                    }
                )
            }
    ) {
        Text("📄 ${node.file.name}")
    }
}