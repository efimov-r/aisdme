package non.era.aisdme.ui.components.contexmenu

import androidx.compose.runtime.*
import non.era.aisdme.LocalAppController
import non.era.aisdme.files.FileFMOpener
import non.era.aisdme.ui.components.dialogs.ConfirmDialog
import non.era.aisdme.ui.project.LinkNode

@Composable
fun ProjectSymlinkContextMenu(node: LinkNode, content: @Composable () -> Unit) {
    val app = LocalAppController.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ConfirmDialog(
            title = "Delete symlink?",
            message = "Symlink \"${node.file}\" will be deleted without the possibility of recovery.",
            onConfirm = { app.delete(node.file.toPath()) },
            onDismiss = { showDialog = false }
        )
    }

    RightClickArea(
        menuEntries = {
            listOf(
                MenuEntry.Item(
                    title = "Delete",
                    icon = "delete",
                    shortcut = "del"
                ) { showDialog = true },

                MenuEntry.Divider,

                MenuEntry.Item(
                    title = "Open in Dolphin",
                    icon = "npm"
                ) { FileFMOpener.openFileInFileManager(node.file) }
            )
        }
    ) {
        content()
    }
}