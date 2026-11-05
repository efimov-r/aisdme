package non.era.aisdme.ui.project.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import non.era.aisdme.di.AppController
import non.era.aisdme.settings.Hotkeys
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.project.TypeMovementOnTree
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode

fun Modifier.treeKeyEvent(app: AppController, showDelDialog: () -> Unit, showRenameDialog: () -> Unit) =
    onPreviewKeyEvent {
    if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        treeKeyFocusWork(it, app, showDelDialog, showRenameDialog)
    }

private fun treeKeyFocusWork(
    event: KeyEvent,
    app: AppController,
    showDelDialog: ()->Unit,
    showRenameDialog: ()->Unit
): Boolean {
    when {
        Hotkeys.COPY.ifDu(event) -> {
            app.copySelectedToClipboard(false)
            return true
        }

        Hotkeys.CUT.ifDu(event) -> {
            app.copySelectedToClipboard(true)
            return true
        }

        Hotkeys.PASTE.ifDu(event) -> {
            app.dialogController.requestPasteDialog()
            return true
        }

        Hotkeys.DELETE.ifDu(event) -> {
            showDelDialog()
            return true
        }

        Hotkeys.RENAME.ifDu(event) -> {
            showRenameDialog()
            return true
        }

        Hotkeys.SHOW_IN_FILE_MANAGER.ifDu(event) -> {
            app.showSelectedInFileManager()
            return true
        }

        Hotkeys.ENTER.ifDu(event) -> {
            val selected = app.projectController.treeState.getSelectedNodes()
            if(selected.size == 1 && selected[0] is DirNode) {
                (selected[0] as DirNode).openOrClose()
                return true
            }
            if(selected.size == 1 && selected[0] is FileNode) {
                app.openSelectedNodesInEditor(Panels.LEFT)
            }
            return true
        }

        Hotkeys.LEFT.ifDu(event) -> {
            app.openSelectedNodesInEditor(Panels.LEFT)
            return true
        }

        Hotkeys.RIGHT.ifDu(event) -> {
            app.openSelectedNodesInEditor(Panels.RIGHT)
            return true
        }

        Hotkeys.SHIFT_TOP.ifDu(event) -> {
            app.projectController.movementOnTree(TypeMovementOnTree.SHIFT_TOP)
            return true
        }

        Hotkeys.TOP.ifDu(event) -> {
            app.projectController.movementOnTree(TypeMovementOnTree.TOP)
            return true
        }

        Hotkeys.SHIFT_BOTTOM.ifDu(event) -> {
            app.projectController.movementOnTree(TypeMovementOnTree.SHIFT_BOTTOM)
            return true
        }

        Hotkeys.BOTTOM.ifDu(event) -> {
            app.projectController.movementOnTree(TypeMovementOnTree.BOTTOM)
            return true
        }

        else -> return false
    }
}