package non.era.aisdme.ui.components.contexmenu

import androidx.compose.runtime.*
import non.era.aisdme.LocalAppController
import non.era.aisdme.files.FileFMOpener
import non.era.aisdme.ui.components.dialogs.ConfirmDialog
import non.era.aisdme.ui.components.dialogs.FileNameDialog
import non.era.aisdme.ui.project.DirNode

@Composable
fun ProjectDirContextMenu(node: DirNode, content: @Composable () -> Unit) {
    val app = LocalAppController.current
    var showDelDialog by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewDirectoryDialog by remember { mutableStateOf(false) }

    if (showDelDialog) {
        ConfirmDialog(
            title = "Delete directory?",
            message = "Directory \"${node.file}\" will be deleted without the possibility of recovery.",
            onConfirm = { app.deleteRecursive(node.file.toPath()) },
            onDismiss = { showDelDialog = false }
        )
    }

    if (showNewFileDialog) {
        FileNameDialog(
            title = "Create new File",
            initial = "filename",
            onConfirm = {
                app.createFile(node.file.toPath().resolve(it))
            },
            onDismiss = { showNewFileDialog = false }
        )
    }

    if (showNewDirectoryDialog) {
        FileNameDialog(
            title = "Create new Directory",
            initial = "name directory",
            onConfirm = {
                app.createDirectory(node.file.toPath().resolve(it))
            },
            onDismiss = { showNewDirectoryDialog = false }
        )
    }

    RightClickArea(
        menuEntries = {
            listOf(
                MenuEntry.Item(
                    title = "New File",
                    icon = "addFile",
                    shortcut = ""
                ) { showNewFileDialog = true },

                MenuEntry.Item(
                    title = "New Directory",
                    icon = "addDirectory",
                    shortcut = ""
                ) { showNewDirectoryDialog = true },

                MenuEntry.Divider,

                MenuEntry.Item(
                    title = "Cut",
                    icon = "cut",
                    shortcut = "Ctrl + x"
                ) { app.cutNode(node) },

                MenuEntry.Item(
                    title = "Copy",
                    icon = "copy",
                    shortcut = "Ctrl + c"
                ) { app.copyToClipboard(listOf(node.file.toPath()), false) },

                MenuEntry.Item(
                    title = "Paste",
                    icon = "paste",
                    shortcut = "Ctrl + v"
                ) { app.pasteFromClipboard(node.file.toPath()) },

                MenuEntry.Divider,

                MenuEntry.Item(
                    title = "Delete",
                    icon = "delete",
                    shortcut = "del"
                ) { showDelDialog = true },

                MenuEntry.Divider,

                MenuEntry.Item(
                    title = "Open in Dolphin",
                    icon = "npm"
                ) { FileFMOpener.openFileInFileManager(node.file) },

                MenuEntry.Divider,

                MenuEntry.Item(
                    title = "Reload from disk",
                    icon = "buildLoadChanges"
                ) { app.reloadDeerFromDisk(node) },
            )
        }
    ) {
        content()
    }
}