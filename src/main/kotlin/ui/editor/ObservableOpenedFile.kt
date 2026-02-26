package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.BuildConfig
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
    private val _file: File
) { //todo readOnly
    var error: String? by mutableStateOf(null)
    var readOnly by mutableStateOf(true)
    var writeStatus by mutableStateOf(WriteStatus.WRITABLE)
    var loadState by mutableStateOf(true)
    var title by mutableStateOf(recomputeTitle())

    private var _text by mutableStateOf("")
    val text get() = _text


    private var autosaveJob: Job? = null
    private var _savedText by mutableStateOf("")
    private val saveMutex: Mutex = Mutex()

    private fun recomputeTitle() = (_file.name ?: "Untitled") + if (_text != _savedText) "*" else ""

    private fun setSavedText(savedTextIn: String, scope: CoroutineScope) {
        this._savedText = savedTextIn
        scope.launch(Dispatchers.Default) {
            updateTitle()
        }
    }

    private suspend fun updateTitle() {
        val newTitle = recomputeTitle()
        withContext(Dispatchers.Swing) {
            title = newTitle
        }
    }

    fun compareFile(file: File) = (_file == file)

    suspend fun saveTextOnDispose() {
        autosaveJob?.cancel()
        saveMutex.withLock {
            runCatching {
                if(_file.canWrite()) {
                    _file.writeText(_text)
                }
            }
        }
    }

    suspend fun openFile() {
        runCatching {
            if(BuildConfig.DEBUG) delay(300)

            val textInit = if (_file.exists()) _file.readText() else ""
            withContext(Dispatchers.Swing) {
                _savedText = textInit
                _text = textInit
                readOnly = false
                loadState = false
            }
        }.getOrElse {
            withContext(Dispatchers.Swing) {
                error = it.message
            }
        }
    }

    fun updateTextAndAutosave(newText: String, scope: CoroutineScope) {
        _text = newText
        scope.launch {
            updateTitle()
        }

        autosaveJob?.cancel()

        autosaveJob = scope.launch {
            delay(2000)
            if (_text == _savedText) return@launch
            if (!_file.canWrite()) return@launch

            runCatching {
                saveMutex.withLock {
                    if(_file.canWrite()) {
                        _file.writeText(_text)
                    }
                }
                withContext(Dispatchers.Swing){
                    error = null
                    setSavedText(_text, scope)
                }

            }.getOrElse {
                withContext(Dispatchers.Swing){
                    error = it.message
                }
            }
        }
    }

    companion object {
        private var nextTabId = AtomicInteger(0)

        fun createEmpty(file: File): ObservableOpenedFile{
            return ObservableOpenedFile(
                id = nextTabId.getAndIncrement(),
                _file = file,
            )
        }
    }
}