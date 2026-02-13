package non.era.aisdme.ui.components.contexmenu

import androidx.compose.runtime.*
import non.era.aisdme.Hotkeys
import non.era.aisdme.LocalAppController
import non.era.aisdme.files.FileFMOpener
import non.era.aisdme.ui.components.dialogs.DeleteNodeDialog
import non.era.aisdme.ui.components.dialogs.NewNodeDialog
import non.era.aisdme.ui.components.dialogs.TypeNew
import non.era.aisdme.ui.project.DirNode

@Composable
fun ProjectDirContextMenu(node: DirNode, content: @Composable () -> Unit) {
    val app = LocalAppController.current

    var showDelDialog by remember { mutableStateOf(false) }
    if (showDelDialog) DeleteNodeDialog(app, node) { showDelDialog = false }

    var showNewFileDialog by remember { mutableStateOf(false) }
    if (showNewFileDialog) NewNodeDialog(app, node, TypeNew.FILE) { showNewFileDialog = false }

    var showNewDirectoryDialog by remember { mutableStateOf(false) }
    if (showNewDirectoryDialog) NewNodeDialog(app, node, TypeNew.DIRECTORY) { showNewDirectoryDialog = false }

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
                ) { app.copyToClipboard(listOf(node.getFilePath()), false) },

                MenuEntry.Item(
                    title = "Paste",
                    icon = "paste",
                    shortcut = Hotkeys.PASTE
                ) { app.pasteFromClipboard(node.getFilePath()) },

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
                ) { FileFMOpener.openFileInFileManager(node.getFile()) },

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