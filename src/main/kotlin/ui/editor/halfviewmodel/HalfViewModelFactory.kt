package non.era.aisdme.ui.editor.halfviewmodel

import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

object HalfViewModelFactory {
    private var nextTabId = AtomicInteger(0)

    fun createObservableOpenedFile(file: File, extScope: CoroutineScope): ObservableOpenedHalfFile{
        return ObservableOpenedHalfFile(
            id = nextTabId.getAndIncrement(),
            _file = file,
            extScope = extScope
        )
    }

    fun createObservableOpenedFile(id: Int, filePath: String, extScope: CoroutineScope): ObservableOpenedHalfFile{
        if(id >= nextTabId.get()){
            nextTabId.set(id+1)
        }
        return ObservableOpenedHalfFile(
            id = id,
            _file = File(filePath),
            extScope = extScope
        )
    }
}