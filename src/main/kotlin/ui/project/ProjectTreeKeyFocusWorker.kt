package non.era.aisdme.ui.project

import androidx.compose.ui.input.key.KeyEvent
import non.era.aisdme.AppController
import non.era.aisdme.Hotkeys
import non.era.aisdme.ui.editor.Panels

fun treeKeyFocusWork(
    event: KeyEvent,
    app: AppController,
    showDelDialog: ()->Unit,
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
            app.pasteToSelectedNode()
            return true
        }

        Hotkeys.DELETE.ifDu(event) -> {
            showDelDialog()
            return true
        }

        Hotkeys.OPEN_DOLPHIN.ifDu(event) -> {
            app.openSelectedInFileManager()
            return true
        }

        Hotkeys.ENTER.ifDu(event) -> {
            val selected = app.getSelectedNodes()
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
            app.movementOnTree(TypeMovementOnTree.SHIFT_TOP)
            return true
        }

        Hotkeys.TOP.ifDu(event) -> {
            app.movementOnTree(TypeMovementOnTree.TOP)
            return true
        }

        Hotkeys.SHIFT_BOTTOM.ifDu(event) -> {
            app.movementOnTree(TypeMovementOnTree.SHIFT_BOTTOM)
            return true
        }

        Hotkeys.BOTTOM.ifDu(event) -> {
            app.movementOnTree(TypeMovementOnTree.BOTTOM)
            return true
        }

        else -> return false
    }
}