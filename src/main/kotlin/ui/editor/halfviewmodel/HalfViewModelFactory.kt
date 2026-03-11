package non.era.aisdme.ui.editor.halfviewmodel

import kotlinx.coroutines.CoroutineScope
import non.era.aisdme.ui.win.WindowState
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

object HalfViewModelFactory {
    val clipboarTypePath = "clipboard" //todo перейти на класс
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

    fun createObservableOpened(id: Int, filePath: String, extScope: CoroutineScope, winState: WindowState): TabCash{
        if(id >= nextTabId.get()){
            nextTabId.set(id+1)
        }
        when(filePath) {
            clipboarTypePath -> {
                return TabCashPaste(
                    id = id,
                    extScope = extScope,
                    winState = winState
                )
            }
            else -> {
                return TabCashTextFile(
                    id = id,
                    _file = File(filePath),
                    extScope = extScope
                )
            }
        }
    }
}