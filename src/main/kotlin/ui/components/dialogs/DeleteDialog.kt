package non.era.aisdme.ui.components.dialogs

import androidx.compose.runtime.Composable
import non.era.aisdme.AppController
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.LinkNode

@Composable
fun DeleteSelectedNodeDialog(app: AppController, onDismiss: () -> Unit) {
    val nodes = app.getSelectedNodes()
    if(nodes.size == 1) {
        when (nodes[0]) {
            is LinkNode -> {
                ConfirmDialog(
                    title = "Delete symlink?",
                    message = "Symlink \"${nodes[0].getFileString()}\" will be deleted without the possibility of recovery.",
                    onConfirm = { app.deleteSelected() },
                    onDismiss = onDismiss
                )
            }
            is DirNode -> {
                ConfirmDialog(
                    title = "Delete directory?",
                    message = "Directory \"${nodes[0].getFileString()}\" will be deleted without the possibility of recovery.",
                    onConfirm = { app.deleteSelected() },
                    onDismiss = onDismiss
                )
            }
            is FileNode -> {
                ConfirmDialog(
                    title = "Delete file?",
                    message = "File \"${nodes[0].getFileString()}\" will be deleted without the possibility of recovery.",
                    onConfirm = { app.deleteSelected() },
                    onDismiss = onDismiss
                )
            }
        }
    } else {
        var nodesText = "\n"
        nodes.forEach { node ->
            nodesText += (node.getFileString() + "\n")
        }
        ConfirmDialog(
            title = "Delete objects?",
            message = "Objects: $nodesText will be deleted without the possibility of recovery.",
            onConfirm = { app.deleteSelected() },
            onDismiss = onDismiss
        )
    }
}