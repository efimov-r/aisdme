package non.era.aisdme.ui.components.contexmenu.realisation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import non.era.aisdme.di.AppController
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.files.work.FileDolphinOpener
import non.era.aisdme.settings.Hotkeys
import non.era.aisdme.ui.components.contexmenu.ContextMenu
import non.era.aisdme.ui.components.contexmenu.ContextMenuState
import non.era.aisdme.ui.components.contexmenu.MenuEntry
import non.era.aisdme.ui.components.dialogs.TypeNew
import non.era.aisdme.ui.components.dialogs.elements.UiDialog
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode

@Composable
fun ProjectTreeContextMenu(content: @Composable BoxScope.() -> Unit) {
    val app = LocalAppController.current
    val menuState = remember { ContextMenuState() }
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(Modifier
        .onPreviewKeyEvent {
            if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

            when (it.key.keyCode) {
                4294967821L -> {
                    app.projectController.treeState.getSelectedNodes().lastOrNull()?.let { node ->
                        node.globPos?.let{ menuState.show(it) }
                    }
                    true
                }
                else -> false
            }
        }
        .onGloballyPositioned { coords = it }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()

                    if (
                        event.type == PointerEventType.Press &&
                        event.buttons.isSecondaryPressed
                    ) {
                        val local = event.changes.first().position
                        val windowPos = coords?.localToWindow(local)

                        windowPos?.let {
                            if(app.projectController.treeState.getSelectedNodes().isNotEmpty()) {
                                menuState.show(it)
                            }
                        }
                    }
                }
            }
        }
        ) {
        content()

        if (menuState.isVisible) {
            TreeContextMenu(
                state = menuState,
                showDelDialog = { app.dialogController.requestDialog(UiDialog.ShowDeleteDialog) },
                showRenameDialog = { app.dialogController.requestDialog(UiDialog.ShowRenameDialog) },
                showNewFileDialog = { app.dialogController.requestDialog(UiDialog.ShowCreateObjDialog(TypeNew.FILE)) },
                showNewDirectoryDialog = { app.dialogController.requestDialog(UiDialog.ShowCreateObjDialog(TypeNew.DIRECTORY)) },
                onDismiss = { menuState.hide() }
            )
        }
    }
}

@Composable
private fun TreeContextMenu(
    state: ContextMenuState,
    showDelDialog: () -> Unit,
    showRenameDialog: () -> Unit,
    showNewFileDialog: () -> Unit,
    showNewDirectoryDialog: () -> Unit,
    onDismiss: () -> Unit
) {
    val app = LocalAppController.current
    val selected = app.projectController.treeState.getSelectedNodes()

    buildList {
        if(isSingleDirectory(selected)) {
            add(newFileItem { showNewFileDialog() })
            add(newDirectoryItem { showNewDirectoryDialog() })
            add(divider())
        }

        add(cutItem(app))
        add(copyItem(app))

        if(isSingleDirectory(selected))
            add(pasteItem(app))

        add( deleteItem {
            showDelDialog()
        } )
        if(isSingle(selected)) {
            add( renameItem {
                showRenameDialog()
            } )
        }
        add(divider())

        if(isOnlyFile(selected)) {
            add(openInLeftItem(app))
            add(openInRightItem(app))
            add(divider())
        }

        add(openDolphinItem(app))

        if(isSingleDirectory(selected)) {
            add(divider())
            add(reloadDiskItem(app))
        }
    }.let {
        ContextMenu(
            entries = it,
            state = state,
            onDismiss = onDismiss
        )
    }
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
) { app.dialogController.requestDialog(UiDialog.ShowPasteDialog) }

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

private fun renameItem(onClick: () -> Unit) = MenuEntry.Item(
    title = "Rename",
    icon = null,
    shortcut = Hotkeys.RENAME,
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
) { FileDolphinOpener.openNodesInFileManager(app.projectController.treeState.getSelectedNodes()) }

private fun reloadDiskItem(app: AppController) = MenuEntry.Item(
    title = "Reload from disk",
    icon = "buildLoadChanges"
) { app.projectController.reloadSelectedDirFromDisk() }

private fun divider() = MenuEntry.Divider

private fun isSingle(selected: List<ProjectNode>) = selected.size == 1
private fun isSingleDirectory(selected: List<ProjectNode>) = selected.size == 1 && selected[0] is DirNode
private fun isSingleFile(selected: List<ProjectNode>) = selected.size == 1 && selected[0] is FileNode
private fun isOnlyFile(selected: List<ProjectNode>) = selected.filterNot { it is FileNode }.isEmpty()