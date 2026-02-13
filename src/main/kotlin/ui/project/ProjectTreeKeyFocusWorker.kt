package non.era.aisdme.ui.project

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import non.era.aisdme.AppController
import non.era.aisdme.Hotkeys

fun treeKeyFocusWork(event: KeyEvent, app: AppController): Boolean {
    val selected = app.getProjectState().getSelectedNode()
    when {
        Hotkeys.COPY.ifDu(event) -> {
            if(selected is FileNode || selected is DirNode) {
                app.copyToClipboard(listOf(selected.file.toPath()), false)
                return true
            }
            return false
        }

        Hotkeys.PASTE.ifDu(event) -> {
            if(selected is DirNode) {
                app.pasteFromClipboard(selected.file.toPath())
                return true
            }
            return false
        }

        //todo вырезка-вставка папки падает в ошибку
        Hotkeys.CUT.ifDu(event) -> {
            if(selected is FileNode || selected is DirNode) {
                app.cutNode(selected)
                return true
            }
            return false
        }

        //todo удаленная директория торчит в списке (не обновляется)
        Hotkeys.DELETE.ifDu(event) -> { //todo dialog
            if (selected is FileNode) {
                app.delete(selected.file.toPath())
                return true
            }
            if(selected is DirNode) {
                app.deleteRecursive(selected.file.toPath())
                return true
            }
            if(selected is LinkNode) {
                app.delete(selected.file.toPath())
                return true
            }
            return false
        }

//        event.key == Key.Enter || event.key == Key.NumPadEnter -> {
//            return true
//        }
//        event.key == Key.Escape -> {
//            return true
//        }
        else -> return false
    }
}