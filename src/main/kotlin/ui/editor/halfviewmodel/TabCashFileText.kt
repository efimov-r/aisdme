package non.era.aisdme.ui.editor.halfviewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.BuildConfig
import non.era.aisdme.bus.ShowFileInTree
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.configs.dataclass.snapshots.TypeTabSnapshot
import java.io.File
import java.nio.file.Files

enum class WriteStatus(val text: String, val back: Color, val te: Color, val toConfig: Boolean, val readOnly: Boolean) {
    LOAD("load", Color(0xFFaca700), Color.Black, true, true),
    NOT_SAVE("save not", Color(0xFFffc908), Color.Black, true, false),
    WRITABLE("writable", Color(0xFF06c000), Color.Black, true, false),
    READ_ONLY_FS("read only", Color(0xFFffc908), Color.Black, true, true), //!Files.isWritable(getFilepath())
    LOCKED("locked", Color(0xFFf38100), Color.Black, false, true),//error write
    ERROR("error", Color(0xFFe20300), Color.Black, false, true) //!file.exists()
}

class TabCashTextFile (
    id: Int,
    private var _file: File,
    extScope: CoroutineScope,
) : TabCash(id, extScope) {
    private var _textFieldValue by mutableStateOf(TextFieldValue(""))
    val textFieldValue: TextFieldValue get() = _textFieldValue

    private var _writeStatus by mutableStateOf(WriteStatus.LOAD)
    val writeStatus get() = _writeStatus

    private var _path by mutableStateOf(_file.toPath())
    val path get() = _path

    override var _name by mutableStateOf(_file.name)

    private var autosaveJob: Job? = null
    private var _savedText by mutableStateOf("")
    private val saveMutex: Mutex = Mutex()

    override fun refile(file: File) {
        _file = file
        _path = _file.toPath()
        _name = _file.name
    }
    fun getFile() = _file
    fun compareFile(file: File) = (_file == file)

    fun showFileInTreeEvent() = ShowFileInTree(_file)

    override fun copy() : TabCashTextFile { //копия для конфига
        val res = TabCashTextFile(id, File(_file.path), _extScope)
        res._textFieldValue = _textFieldValue
        res._writeStatus = _writeStatus
        res._savedText = _savedText
        return res
    }

    override suspend fun disposeBeforeBlocked() {
        autosaveJob?.cancel()
        saveMutex.withLock {
            runCatching {
                _file.writeText(_textFieldValue.text)
            }
        }
    }

    override suspend fun openFile(typeOpen: OpenFileType) {
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
                _textFieldValue = TextFieldValue(textIn)
            }
        } catch(_: Throwable) {
            writeStatus(WriteStatus.LOCKED)
        }
    }

    override fun toSnapshot(): SnapshotTabCash? {
        if(!writeStatus.toConfig) return null
        return SnapshotTabCash(_file.path, TypeTabSnapshot.TEXT_FILE)
    }

    fun updateTextFieldValue(newValue: TextFieldValue) {
        _textFieldValue = newValue
        _writeStatus = WriteStatus.NOT_SAVE

        autosaveJob?.cancel()
        autosaveJob = _extScope.launch {
            delay(2000)
            saveNow()
        }
    }

    override suspend fun beforeRemove() {
        saveNow()
    }

    private suspend fun saveNow() {
        if (_textFieldValue.text == _savedText) return

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
                _file.writeText(_textFieldValue.text)
                _savedText = _textFieldValue.text
            }
            writeStatus(WriteStatus.WRITABLE)
        } catch (_: Throwable) {
            writeStatus(WriteStatus.LOCKED)
        }
    }

    private suspend fun writeStatus(status: WriteStatus) {
        withContext(Dispatchers.Swing) {
            _writeStatus = status
        }
    }
}