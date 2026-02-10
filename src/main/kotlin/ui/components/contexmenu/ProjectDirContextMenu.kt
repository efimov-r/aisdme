package non.era.aisdme.ui.components.contexmenu

import androidx.compose.runtime.*
import non.era.aisdme.LocalAppController
import non.era.aisdme.files.FileFMOpener
import non.era.aisdme.ui.components.dialogs.ConfirmDialog
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.project.DirNode

@Composable
fun ProjectDirContextMenu(node: DirNode, content: @Composable () -> Unit) {
    val app = LocalAppController.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ConfirmDialog(
            title = "Delete directory?",
            message = "Directory \"${node.file}\" will be deleted without the possibility of recovery.",
            onConfirm = { app.delete(node.file.toPath()) },
            onDismiss = { showDialog = false }
        )
    }

    RightClickArea(
        menuEntries = {
            listOf(
                //todo new

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
                ) { showDialog = true },

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