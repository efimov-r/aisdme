package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicIntegerArray

class ObservableOpenedFile private constructor(
    val id: Int,
    val file: File,
    textIn: String,
    savedTextIn: String,
    errorIn: String? = null,
) { //todo readOnly
    private val saveMutex: Mutex = Mutex()
    var autosaveJob: Job? = null
    var error by mutableStateOf(errorIn)
    var readOnly by mutableStateOf(true)
    private var text_ by mutableStateOf(textIn)
    private var savedText_ by mutableStateOf(savedTextIn)
    var title by mutableStateOf(recomputeTitle())

    val isModified: Boolean
        get() = text_ != savedText_

    private fun recomputeTitle() = (file?.name ?: "Untitled") + if (isModified) "*" else ""

    fun setSavedText(savedTextIn: String, scope: CoroutineScope) {
        this.savedText_ = savedTextIn
        scope.launch(Dispatchers.Default) {
            val newTitle = recomputeTitle()
            withContext(Dispatchers.Swing) {
                title = newTitle
            }
        }
    }

    fun initText(savedTextIn: String) {
        this.savedText_ = savedTextIn
        this.text_ = savedTextIn
    }

    fun setText(textIn: String) {
        this.text_ = textIn
    }

    suspend fun updateTitle() {
        val newTitle = recomputeTitle()
        withContext(Dispatchers.Swing) {
            title = newTitle
        }
    }

    fun getText() = text_

    fun fileNotCanWrite() = !file.canWrite()

    suspend fun writeTextToFile() {
        saveMutex.withLock {
            file.writeText(text_)
        }
    }

    companion object {
        private var nextTabId = AtomicInteger(0)
        private val nextTabIdMutex = Mutex()

        fun createNew(file: File, initText: String, errorIn: String?): ObservableOpenedFile{
            return ObservableOpenedFile(
                id = nextTabId.getAndIncrement(),
                file = file,
                textIn = initText,
                savedTextIn = initText,
                errorIn = errorIn
            )
        }

        fun createEmpty(file: File): ObservableOpenedFile{
            val t = ""
            return ObservableOpenedFile(
                id = nextTabId.getAndIncrement(),
                file = file,
                textIn = t,
                savedTextIn = t,
                errorIn = null
            )
        }
    }
}