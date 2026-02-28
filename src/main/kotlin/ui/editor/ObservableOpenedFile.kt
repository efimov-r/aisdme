package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.BuildConfig
import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger

enum class WriteStatus(val text: String, val back: Color, val te: Color, val toConfig: Boolean) {
    LOAD("load", Color(0xFFaca700), Color.Black, true),
    NOT_SAVE("save not", Color(0xFFffc908), Color.Black, true),
    WRITABLE("writable", Color(0xFF06c000), Color.Black, true),
    READ_ONLY_FS("read only", Color(0xFFffc908), Color.Black, true), //!Files.isWritable(getFilepath())
    LOCKED("locked", Color(0xFFf38100), Color.Black, false),//error write
    ERROR("error", Color(0xFFe20300), Color.Black, false) //!file.exists()
}

class ObservableOpenedFile private constructor(
    val id: Int,
    private val _file: File
) {
    private var _text by mutableStateOf("")
    val text get() = _text

    private var _writeStatus by mutableStateOf(WriteStatus.LOAD)
    val writeStatus get() = _writeStatus

    private var _title by mutableStateOf(_file.name ?: "Untitled")
    val title get() = _title

    private var _path by mutableStateOf(_file.path ?: "")
    val path get() = _path

    private var autosaveJob: Job? = null
    private var _savedText by mutableStateOf("")
    private val saveMutex: Mutex = Mutex()

    fun getFilePath() = _file.toPath().toString()
    fun compareFile(file: File) = (_file == file)

    fun copy() : ObservableOpenedFile { //копия для конфига
        val res = ObservableOpenedFile(id, File(_file.path))
        res._text = _text
        res._writeStatus = _writeStatus
        res._title = _title
        res._path = _path
        res._savedText = _savedText
        return res
    }

    suspend fun saveTextOnDispose() {
        autosaveJob?.cancel()
        saveMutex.withLock {
            runCatching {
                _file.writeText(_text)
            }
        }
    }

    suspend fun openFile() {
        if(BuildConfig.DEBUG) delay(300)

        if(!_file.exists()) {
            writeStatus(WriteStatus.ERROR)
            return
        }

        try {
            val textIn = _file.readText()
            withContext(Dispatchers.Swing) {
                _writeStatus =
                    if(!Files.isWritable(_file.toPath()))  WriteStatus.READ_ONLY_FS else WriteStatus.WRITABLE
                _savedText = textIn
                _text = textIn
            }
        } catch(_: Throwable) {
            writeStatus(WriteStatus.LOCKED)
        }
    }

    fun updateTextAndAutosave(newText: String, scope: CoroutineScope) {
        _text = newText
        _writeStatus = WriteStatus.NOT_SAVE

        autosaveJob?.cancel()

        autosaveJob = scope.launch {
            delay(2000)
            saveNow()
        }
    }

    suspend fun saveNow() {
        if (_text == _savedText) return

        if(!_file.exists()) {
            writeStatus(WriteStatus.ERROR)
            return
        }

        if(!Files.isWritable(_file.toPath())) {
            writeStatus(WriteStatus.READ_ONLY_FS)
            return
        }

        try{
            saveMutex.withLock {
                _file.writeText(_text)
                _savedText = _text
            }
            writeStatus(WriteStatus.WRITABLE)
        } catch (_: Throwable) {
            writeStatus(WriteStatus.LOCKED)
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

        fun createFromConfig(id: Int, filePath: String): ObservableOpenedFile{
            if(id >= nextTabId.get()){
                nextTabId.set(id+1)
            }
            return ObservableOpenedFile(
                id = id,
                _file = File(filePath),
            )
        }
    }

    private suspend fun writeStatus(status: WriteStatus) {
        withContext(Dispatchers.Swing) {
            _writeStatus = status
        }
    }
}