package non.era.aisdme.ui.components.contexmenu

import androidx.compose.runtime.*
import non.era.aisdme.Hotkeys
import non.era.aisdme.LocalAppController
import non.era.aisdme.files.FileFMOpener
import non.era.aisdme.ui.components.dialogs.DeleteNodeDialog
import non.era.aisdme.ui.project.LinkNode

@Composable
fun ProjectSymlinkContextMenu(node: LinkNode, content: @Composable () -> Unit) {
    val app = LocalAppController.current

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) DeleteNodeDialog(app, node) { showDialog = false }

    RightClickArea(
        menuEntries = {
            listOf(
                MenuEntry.Item(
                    title = "Delete",
                    icon = "delete",
                    shortcut = Hotkeys.DELETE
                ) { showDialog = true },

                MenuEntry.Divider,

                MenuEntry.Item(
                    title = "Open in Dolphin",
                    icon = "npm",
                    shortcut = Hotkeys.OPEN_DOLPHIN
                ) { FileFMOpener.openFileInFileManager(node.getFile()) }
            )
        }
    ) {
        content()
    }
}