package non.era.aisdme.ui.components.contexmenu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import non.era.aisdme.AppController
import non.era.aisdme.Hotkeys
import non.era.aisdme.LocalAppController
import non.era.aisdme.Log
import non.era.aisdme.ui.components.dialogs.DeleteSelectedNodeDialog
import non.era.aisdme.ui.components.dialogs.NewNodeDialog
import non.era.aisdme.ui.components.dialogs.TypeNew
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.project.DirNode
import non.era.aisdme.ui.project.FileNode
import non.era.aisdme.ui.project.ProjectNode

@Composable
fun ProjectTreeContextMenu(content: @Composable BoxScope.() -> Unit) {
    val menuState = remember { ContextMenuState() }
    Box(Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (
                    event.type == PointerEventType.Press &&
                    event.buttons.isSecondaryPressed
                ) {
                    menuState.show(event.changes.first().position)
                }
            }
        }
    }) {
        content()

        if (menuState.isVisible) {
            TreeContextMenu(menuState) {
                menuState.hide()
            }
        }
    }
}

@Composable
private fun TreeContextMenu(state: ContextMenuState, onDismiss: () -> Unit) {
    val app = LocalAppController.current
    val selected = app.getSelectedNodes()
    val listItems: MutableList<MenuEntry> = mutableListOf()
    Log.d("recompose context menu")

    var showDelDialog by remember { mutableStateOf(false) }
    if (showDelDialog) DeleteSelectedNodeDialog(app) { showDelDialog = false }

    var showNewFileDialog by remember { mutableStateOf(false) }
    if (showNewFileDialog) NewNodeDialog(app, TypeNew.FILE) { showNewFileDialog = false }

    var showNewDirectoryDialog by remember { mutableStateOf(false) }
    if (showNewDirectoryDialog) NewNodeDialog(app, TypeNew.DIRECTORY) { showNewDirectoryDialog = false }


    if(isSingleDirectory(selected)) {
        listItems.add(newFileItem { showNewFileDialog = true })
        listItems.add(newDirectoryItem { showNewDirectoryDialog = true })
        listItems.add(divider())
    }

    listItems.add(cutItem(app))
    listItems.add(copyItem(app))

    if(isSingleDirectory(selected))
        listItems.add(pasteItem(app))

    listItems.add( deleteItem { showDelDialog = true } )
    listItems.add(divider())

    if(isSingleFile(selected)) {
        listItems.add(openInLeftItem(app))
        listItems.add(openInRightItem(app))
        listItems.add(divider())
    }

    listItems.add(openDolphinItem(app))

    if(isSingleDirectory(selected)) {
        listItems.add(divider())
        listItems.add(reloadDiskItem(app))
    }

    ContextMenu(
        entries = listItems,
        state = state,
        onDismiss = onDismiss
    )
}

private fun newDirectoryItem(onClick: () -> Unit) = MenuEntry.Item(
    title = "New Directory",
    icon = "addDirectory",
    onClick = onClick
)

private fun newFileItem(onClick: () -> Unit) = MenuEntry.Item(
    title = "New File",
    icon = "addFile",
    onClick = onClick
)

private fun pasteItem(app: AppController) = MenuEntry.Item(
    title = "Paste",
    icon = "paste",
    shortcut = Hotkeys.PASTE
) { app.pasteToSelectedNode() }

private fun copyItem(app: AppController) = MenuEntry.Item(
    title = "Copy",
    icon = "copy",
    shortcut = Hotkeys.COPY
) { app.copySelectedToClipboard(false) }

private fun cutItem(app: AppController) = MenuEntry.Item(
    title = "Cut",
    icon = "cut",
    shortcut = Hotkeys.CUT
) { app.copySelectedToClipboard(true) }

private fun deleteItem(onClick: () -> Unit) = MenuEntry.Item(
    title = "Delete",
    icon = "delete",
    shortcut = Hotkeys.DELETE,
    onClick = onClick
)

private fun openInLeftItem(app: AppController) = MenuEntry.Item(
    title = "Open in Left",
    icon = "applyNotConflictsRight",
    shortcut = Hotkeys.LEFT
) { app.openSelectedNodesInEditor(Panels.LEFT) }

private fun openInRightItem(app: AppController) = MenuEntry.Item(
    title = "Open in Right",
    icon = "applyNotConflictsLeft",
    shortcut = Hotkeys.RIGHT
) { app.openSelectedNodesInEditor(Panels.RIGHT) }

private fun openDolphinItem(app: AppController) = MenuEntry.Item(
    title = "Open in Dolphin",
    icon = "npm",
    shortcut = Hotkeys.OPEN_DOLPHIN
) { app.openSelectedInFileManager() }

private fun reloadDiskItem(app: AppController) = MenuEntry.Item(
    title = "Reload from disk",
    icon = "buildLoadChanges"
) { app.reloadSelectedDeerFromDisk() }

private fun divider() = MenuEntry.Divider

private fun isSingleDirectory(selected: List<ProjectNode>) = selected.size == 1 && selected[0] is DirNode
private fun isSingleFile(selected: List<ProjectNode>) = selected.size == 1 && selected[0] is FileNode