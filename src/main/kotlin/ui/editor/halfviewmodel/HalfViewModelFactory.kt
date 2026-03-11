package non.era.aisdme.ui.editor.halfviewmodel

import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

object HalfViewModelFactory {
    val clipboarTypePath = "clipboard" //todo перейти на класс
    private var nextTabId = AtomicInteger(0)

    fun clearNexTabId() {
        nextTabId.set(0)
    }

    fun createObservableOpenedPaste(extScope: CoroutineScope) =
        ObservableOpenedHalfPaste(nextTabId.getAndIncrement(), extScope)


    fun createObservableOpenedFile(file: File, extScope: CoroutineScope) =
        ObservableOpenedHalfFile(
            id = nextTabId.getAndIncrement(),
            _file = file,
            extScope = extScope
        )

    fun createObservableOpened(id: Int, filePath: String, extScope: CoroutineScope): ObservableOpenedHalf{
        if(id >= nextTabId.get()){
            nextTabId.set(id+1)
        }
        when(filePath) {
            clipboarTypePath -> {
                return ObservableOpenedHalfPaste(
                    id = id,
                    extScope = extScope
                )
            }
            else -> {
                return ObservableOpenedHalfFile(
                    id = id,
                    _file = File(filePath),
                    extScope = extScope
                )
            }
        }
    }
}