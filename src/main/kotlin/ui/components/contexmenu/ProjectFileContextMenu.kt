package non.era.aisdme.ui.components.contexmenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import non.era.aisdme.Hotkeys
import non.era.aisdme.LocalAppController
import non.era.aisdme.files.FileFMOpener
import non.era.aisdme.ui.components.dialogs.ConfirmDialog
import non.era.aisdme.ui.components.dialogs.DeleteNodeDialog
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.project.FileNode


@Composable
fun ProjectFileContextMenu(node: FileNode, content: @Composable () -> Unit) {
    val app = LocalAppController.current

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) DeleteNodeDialog(app, node) { showDialog = false }

    RightClickArea(
        menuEntries = {
            listOf(
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
                    title = "Delete",
                    icon = "delete",
                    shortcut = Hotkeys.DELETE
                ) { showDialog = true },

                MenuEntry.Divider,

                MenuEntry.Item(
                    title = "Open in Left",
                    icon = "applyNotConflictsRight",
                    shortcut = Hotkeys.LEFT
                ) { app.openFileInEditor(node.file, Panels.LEFT) },

                MenuEntry.Item(
                    title = "Open in Right",
                    icon = "applyNotConflictsLeft",
                    shortcut = Hotkeys.RIGHT
                ) { app.openFileInEditor(node.file, Panels.RIGHT) },

                MenuEntry.Divider,

                MenuEntry.Item(
                    title = "Open in Dolphin",
                    icon = "npm",
                    shortcut = Hotkeys.OPEN_DOLPHIN
                ) { FileFMOpener.openFileInFileManager(node.file) }
            )
        }
    ) {
        content()
    }
}

