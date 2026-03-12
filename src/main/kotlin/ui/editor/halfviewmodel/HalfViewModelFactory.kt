package non.era.aisdme.ui.editor.halfviewmodel

import kotlinx.coroutines.CoroutineScope
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.configs.dataclass.snapshots.TypeTabSnapshot
import non.era.aisdme.ui.win.WindowState
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

object HalfViewModelFactory {
    private var nextTabId = AtomicInteger(0)

    fun clearNexTabId() {
        nextTabId.set(0)
    }

    fun createObservableOpenedPaste(extScope: CoroutineScope, winState: WindowState) =
        TabCashPaste(nextTabId.getAndIncrement(), extScope, winState)


    fun createObservableOpenedFile(file: File, extScope: CoroutineScope) =
        TabCashTextFile(
            id = nextTabId.getAndIncrement(),
            _file = file,
            extScope = extScope
        )

    fun createObservableOpened(id: Int, snap: SnapshotTabCash, extScope: CoroutineScope, winState: WindowState): TabCash? {
        if(id >= nextTabId.get()){
            nextTabId.set(id+1)
        }
        when(snap.type) {
            TypeTabSnapshot.PASTE_TAB -> {
                return TabCashPaste(
                    id = id,
                    extScope = extScope,
                    winState = winState
                )
            }
            TypeTabSnapshot.TEXT_FILE -> {
                return snap.pathStr?.let { pathStr ->
                    TabCashTextFile(
                        id = id,
                        _file = File(pathStr),
                        extScope = extScope
                    )
                }
            }
            null -> return null
        }
    }
}