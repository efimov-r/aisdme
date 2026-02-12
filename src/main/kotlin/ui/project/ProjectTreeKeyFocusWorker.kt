package non.era.aisdme.ui.project

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import non.era.aisdme.AppController

fun treeKeyFocusWork(event: KeyEvent, app: AppController): Boolean {
    val selected = app.getProjectState().getSelectedNode()
    when {
        event.isCtrlPressed && event.key == Key.C -> {
            if(selected is FileNode || selected is DirNode) {
                app.copyToClipboard(listOf(selected.file.toPath()), false)
                return true
            }
            return false
        }

        event.isCtrlPressed && event.key == Key.V -> {
            if(selected is DirNode) {
                app.pasteFromClipboard(selected.file.toPath())
                return true
            }
            return false
        }

        //todo вырезка-вставка папки падает в ошибку
        event.isCtrlPressed && event.key == Key.X -> {
            if(selected is FileNode || selected is DirNode) {
                app.cutNode(selected)
                return true
            }
            return false
        }

        //todo удаленная директория торчит в списке (не обновляется)
        event.key == Key.Delete -> { //todo dialog
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