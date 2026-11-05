package non.era.aisdme.ui.editor.halfviewmodel

import kotlinx.coroutines.CoroutineScope
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.files.TypeFile
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileImg
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashPaste
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText
import non.era.aisdme.ui.win.WindowState
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class HalfViewModelFactory { //своя у каждого EditorController: нумерация вкладок не общая
    private val nextTabId = AtomicInteger(0)

    fun clearNexTabId() {
        nextTabId.set(0)
    }

    fun createPasteTab(extScope: CoroutineScope, winState: WindowState) =
        TabCashPaste(nextTabId.getAndIncrement(), extScope, winState)


    fun createFileTab(file: File, extScope: CoroutineScope): TabCash {
        if(TypeFile.isImageFile(file.toPath())) {
            return TabCashFileImg(
                id = nextTabId.getAndIncrement(),
                file = file,
                extScope = extScope
            )
        }
        return TabCashFileText(
            id = nextTabId.getAndIncrement(),
            file = file,
            extScope = extScope
        )
    }

    fun createTabFromSnapshot(
        id: Int,
        snap: SnapshotTabCash,
        extScope: CoroutineScope,
        winState: WindowState
    ): TabCash? {
        if(id >= nextTabId.get()){
            nextTabId.set(id+1)
        }
        when(snap.type) {
            TypeFile.PASTE_TAB -> {
                return TabCashPaste(
                    id = id,
                    extScope = extScope,
                    winState = winState
                )
            }
            TypeFile.TEXT_FILE -> {
                return snap.pathStr?.let { pathStr ->
                    TabCashFileText(
                        id = id,
                        file = File(pathStr),
                        extScope = extScope
                    )
                }
            }
            TypeFile.IMG_FILE -> {
                return snap.pathStr?.let { pathStr ->
                    TabCashFileImg(
                        id = id,
                        file = File(pathStr),
                        extScope = extScope
                    )
                }
            }
            null -> return null
        }
    }
}