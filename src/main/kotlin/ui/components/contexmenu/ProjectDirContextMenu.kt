package non.era.aisdme.ui.components.contexmenu

import androidx.compose.runtime.*
import non.era.aisdme.Hotkeys
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
                ) { showNewFileDialog = true },

                MenuEntry.Item(
                    title = "New Directory",
                    icon = "addDirectory",
                ) { showNewDirectoryDialog = true },

                MenuEntry.Divider,

                MenuEntry.Item(
                    title = "Cut",
                    icon = "cut",
                    shortcut = Hotkeys.CUT
                ) { app.cutNode(node) },

                MenuEntry.Item(
                    title = "Copy",
                    icon = "copy",
                    shortcut = Hotkeys.COPY
                ) { app.copyToClipboard(listOf(node.file.toPath()), false) },

                MenuEntry.Item(
                    title = "Paste",
                    icon = "paste",
                    shortcut = Hotkeys.PASTE
                ) { app.pasteFromClipboard(node.file.toPath()) },

                MenuEntry.Divider,

                MenuEntry.Item(
                    title = "Delete",
                    icon = "delete",
                    shortcut = Hotkeys.DELETE
                ) { showDelDialog = true },

                MenuEntry.Divider,

                MenuEntry.Item(
                    title = "Open in Dolphin",
                    icon = "npm",
                    shortcut = Hotkeys.OPEN_DOLPHIN
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