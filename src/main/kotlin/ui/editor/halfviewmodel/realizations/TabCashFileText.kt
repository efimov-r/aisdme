package non.era.aisdme.ui.editor.halfviewmodel.realizations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
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
import non.era.aisdme.utils.Log
import java.io.File
import java.nio.file.Files

enum class ExternalChoice { LOAD_FROM_DISK, KEEP_MINE, SAVE_AGAIN } //выбор, когда файл изменили или удалили снаружи

private const val AUTOSAVE_DELAY_MS = 2000L
private const val DISPOSE_WAIT_MS = 1000L

private fun writableStatus(file: File) =
    if (Files.isWritable(file.toPath())) WriteStatus.WRITABLE else WriteStatus.READ_ONLY_FS

private suspend fun readDiskText(file: File) = //null — файл занят другой программой
    withContext(Dispatchers.IO) { runCatching { file.readText() }.getOrNull() }

private suspend fun writeNewFile(file: File, text: String) = withContext(Dispatchers.IO) {
    file.parentFile?.mkdirs()
    file.writeText(text)
}

private fun reloaded(old: TextFieldValue, text: String): TextFieldValue { //выделение не выходит за новый текст
    val sel = old.selection
    return TextFieldValue(text, TextRange(sel.start.coerceAtMost(text.length), sel.end.coerceAtMost(text.length)))
}

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

    private enum class Sync { SAME, RELOADED, CONFLICT, DELETED }

    private val canSave get() = _writeStatus == WriteStatus.WRITABLE || _writeStatus == WriteStatus.NOT_SAVE

    private val canSync get() = canSave || _writeStatus == WriteStatus.READ_ONLY_FS || _writeStatus.awaitsChoice

    override suspend fun disposeBeforeBlocked() { //без writeStatus: вызывается из runBlocking на EDT
        autosaveJob?.cancel()
        if (!canSave) return //не загружен или ошибка: иначе файл затрётся
        val text = _textFieldValue.text
        if (text == _savedText || !_file.exists()) return

        withTimeoutOrNull(DISPOSE_WAIT_MS) { //сверка с диском ждёт ui-поток, а он сам ждёт здесь
            saveMutex.withLock {
                if (readStamp() != diskStamp) return@withLock //файл изменили снаружи: не затираем
                runCatching {
                    _file.writeText(text)
                }
            }
        }
    }

    override suspend fun openExistFile(typeOpen: OpenFileType) {
        val stamp = readStamp() //до чтения: правка во время чтения будет замечена при сверке
        val textIn = _file.readText()
        withContext(Dispatchers.Swing) {
            _writeStatus = writableStatus(_file)
            diskStamp = stamp
            _savedText = textIn
            _textFieldValue = TextFieldValue(textIn)
        }
    }

    fun updateTextFieldValue(newValue: TextFieldValue) {
        if (_writeStatus.awaitsChoice) { //ввод закрыт: клик и выделение не должны сбить статус
            _textFieldValue = newValue
            return
        }
        _textFieldValue = newValue
        _writeStatus = WriteStatus.NOT_SAVE

        scheduleAutosave()
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = _extScope.launch {
            delay(AUTOSAVE_DELAY_MS)
            saveNow()
        }
    }

    override suspend fun beforeRemove() {
        saveNow()
    }

    override suspend fun checkDisk(): Boolean = saveMutex.withLock {
        syncWithDisk() == Sync.DELETED && withContext(Dispatchers.Swing) { _textFieldValue.text == _savedText }
    }

    /** Сверка с диском, вызывать под saveMutex */
    private suspend fun syncWithDisk(): Sync {
        val stamp = readStamp()
        val diskText = if (canSync && stamp != null && stamp != diskStamp) readDiskText(_file) else null

        return when {
            !canSync -> Sync.SAME
            stamp == null -> {
                diskStamp = null //вернут файл — отпечаток точно будет другой
                writeStatus(WriteStatus.DELETED)
                Sync.DELETED
            }
            diskText == null -> Sync.SAME //не менялся или файл занят: проверим в следующий раз
            else -> withContext(Dispatchers.Swing) {
                diskStamp = stamp
                val clean = _textFieldValue.text == _savedText
                when {
                    diskText == _savedText -> { //touch или файл вернули как был
                        if (_writeStatus.awaitsChoice) {
                            _writeStatus = if (clean) writableStatus(_file) else WriteStatus.NOT_SAVE
                        }
                        if (_writeStatus == WriteStatus.NOT_SAVE) scheduleAutosave()
                        Sync.SAME
                    }
                    clean -> { //правок нет: берём текст с диска
                        _savedText = diskText
                        _textFieldValue = reloaded(_textFieldValue, diskText)
                        _writeStatus = writableStatus(_file)
                        Sync.RELOADED
                    }
                    else -> { //есть ввод и файл другой: решает пользователь
                        autosaveJob?.cancel()
                        _writeStatus = WriteStatus.CONFLICT
                        Sync.CONFLICT
                    }
                }
            }
        }
    }

    fun resolve(choice: ExternalChoice) {
        _extScope.launch {
            saveMutex.withLock { resolveLocked(choice) }
        }
    }

    private suspend fun resolveLocked(choice: ExternalChoice) {
        val text = _textFieldValue.text
        when (choice) {
            ExternalChoice.LOAD_FROM_DISK -> { //ввод пропадает
                val stamp = readStamp()
                val diskText = readDiskText(_file)
                if (stamp != null && diskText != null) withContext(Dispatchers.Swing) {
                    diskStamp = stamp
                    _savedText = diskText
                    _textFieldValue = reloaded(_textFieldValue, diskText)
                    _writeStatus = writableStatus(_file)
                }
            }
            ExternalChoice.KEEP_MINE -> withContext(Dispatchers.Swing) { //следующее сохранение перезапишет файл
                if (_writeStatus == WriteStatus.CONFLICT) {
                    _writeStatus = WriteStatus.NOT_SAVE
                    scheduleAutosave()
                }
            }
            ExternalChoice.SAVE_AGAIN -> if (_writeStatus == WriteStatus.DELETED) {
                runCatching {
                    writeNewFile(_file, text)
                    _savedText = text
                    diskStamp = readStamp()
                    writeStatus(writableStatus(_file))
                }.onFailure { Log.e(it) } //статус остаётся DELETED: можно повторить или закрыть вкладку
            }
        }
    }

    private suspend fun saveNow() {
        if (_textFieldValue.text == _savedText) return

        val status = saveMutex.withLock {
            when {
                syncWithDisk() != Sync.SAME -> null //файл изменили или удалили снаружи: не перезаписываем
                !_file.exists() -> WriteStatus.ERROR
                !Files.isWritable(_file.toPath()) -> WriteStatus.READ_ONLY_FS
                else -> {
                    val text = _textFieldValue.text //один раз: ввод во время записи не должен считаться сохранённым
                    try {
                        _file.writeText(text)
                        _savedText = text
                        diskStamp = readStamp()
                        WriteStatus.WRITABLE
                    } catch (_: Throwable) {
                        WriteStatus.LOCKED
                    }
                }
            }
        }
        status?.let { writeStatus(it) }
    }
}
