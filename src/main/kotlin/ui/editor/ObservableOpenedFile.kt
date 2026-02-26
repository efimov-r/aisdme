package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

enum class WriteStatus {
    LOAD,
    WRITABLE,
    READ_ONLY_FS, //!Files.isWritable(getFilepath())
    LOCKED,//error write
    ERROR //!file.exists()
}

class ObservableOpenedFile private constructor(
    val id: Int,
    val file: File
) { //todo readOnly
    var autosaveJob: Job? = null
    var error: String? by mutableStateOf(null)
    var readOnly by mutableStateOf(true)
    var writeStatus by mutableStateOf(WriteStatus.WRITABLE)
    var loadState by mutableStateOf(true)
    var text by mutableStateOf("")
    var title by mutableStateOf(recomputeTitle())

    private var _savedText by mutableStateOf("")
    private val saveMutex: Mutex = Mutex()

    val isModified: Boolean
        get() = text != _savedText

    private fun recomputeTitle() = (file.name ?: "Untitled") + if (isModified) "*" else ""

    fun setSavedText(savedTextIn: String, scope: CoroutineScope) {
        this._savedText = savedTextIn
        scope.launch(Dispatchers.Default) {
            updateTitle()
        }
    }

    fun initText(savedTextIn: String) {
        this._savedText = savedTextIn
        this.text = savedTextIn
    }

    suspend fun updateTitle() {
        val newTitle = recomputeTitle()
        withContext(Dispatchers.Swing) {
            title = newTitle
        }
    }

    suspend fun writeTextToFile() {
        saveMutex.withLock {
            file.writeText(text)
        }
    }

    companion object {
        private var nextTabId = AtomicInteger(0)

        fun createEmpty(file: File): ObservableOpenedFile{
            return ObservableOpenedFile(
                id = nextTabId.getAndIncrement(),
                file = file,
            )
        }
    }
}