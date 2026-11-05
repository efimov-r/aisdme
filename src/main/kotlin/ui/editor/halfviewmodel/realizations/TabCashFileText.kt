package non.era.aisdme.ui.editor.halfviewmodel.realizations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.BuildConfig
import non.era.aisdme.bus.ShowFileInTree
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.files.TypeFile
import non.era.aisdme.ui.editor.halfviewmodel.TabCash
import non.era.aisdme.ui.editor.halfviewmodel.TabCashFile
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import java.io.File
import java.nio.file.Files

class TabCashFileText (
    id: Int,
    file: File,
    extScope: CoroutineScope,
) : TabCashFile(id, file, extScope) {
    private var _textFieldValue by mutableStateOf(TextFieldValue(""))
    val textFieldValue: TextFieldValue get() = _textFieldValue

    override val typeFile: TypeFile = TypeFile.TEXT_FILE

    private var autosaveJob: Job? = null
    private var _savedText by mutableStateOf("")
    private val saveMutex: Mutex = Mutex()

    override fun copy() : TabCashFileText { //копия для конфига
        val res = TabCashFileText(id, File(_file.path), _extScope)
        res._textFieldValue = _textFieldValue
        res._savedText = _savedText
        copyCashFile(res)
        return res
    }

    override suspend fun disposeBeforeBlocked() { //без writeStatus: вызывается из runBlocking на EDT
        autosaveJob?.cancel()
        if (!canSave()) return //не загружен или ошибка: иначе файл затрётся
        val text = _textFieldValue.text
        if (text == _savedText || !_file.exists()) return

        saveMutex.withLock {
            runCatching {
                _file.writeText(text)
            }
        }
    }

    private fun canSave() = _writeStatus == WriteStatus.WRITABLE || _writeStatus == WriteStatus.NOT_SAVE

    override suspend fun openExistFile(typeOpen: OpenFileType) {
        val textIn = _file.readText()
        withContext(Dispatchers.Swing) {
            if(!Files.isWritable(_file.toPath()))
                writeStatus(WriteStatus.READ_ONLY_FS)
            else
                writeStatus(WriteStatus.WRITABLE)
            _savedText = textIn
            _textFieldValue = TextFieldValue(textIn)
        }
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

        val text = _textFieldValue.text //один раз: ввод во время записи не должен считаться сохранённым
        try{
            saveMutex.withLock {
                _file.writeText(text)
                _savedText = text
            }
            writeStatus(WriteStatus.WRITABLE)
        } catch (_: Throwable) {
            writeStatus(WriteStatus.LOCKED)
        }
    }
}