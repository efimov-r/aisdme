package non.era.aisdme.ui.components.dialogs

import androidx.compose.runtime.Composable
import non.era.aisdme.AppController
import non.era.aisdme.ui.project.DirNode
import non.era.aisdme.ui.project.FileNode
import non.era.aisdme.ui.project.LinkNode
import non.era.aisdme.ui.project.ProjectNode

@Composable
fun DeleteNodeDialog(app: AppController, node: ProjectNode, onDismiss: () -> Unit) {
    when (node) {
        is LinkNode -> {
            ConfirmDialog(
                title = "Delete symlink?",
                message = "Symlink \"${node.file}\" will be deleted without the possibility of recovery.",
                onConfirm = { app.delete(node.file.toPath()) },
                onDismiss = onDismiss
            )
        }
        is DirNode -> {
            ConfirmDialog(
                title = "Delete directory?",
                message = "Directory \"${node.file}\" will be deleted without the possibility of recovery.",
                onConfirm = { app.deleteRecursive(node.file.toPath()) },
                onDismiss = onDismiss
            )
        }
        is FileNode -> {
            ConfirmDialog(
                title = "Delete file?",
                message = "File \"${node.file}\" will be deleted without the possibility of recovery.",
                onConfirm = { app.delete(node.file.toPath()) },
                onDismiss = onDismiss
            )
        }
    }

}