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

class ObservableEditorTab private constructor(
    val id: Int,
    val file: File,
    textIn: String,
    savedTextIn: String,
    errorIn: String? = null,
) {
    private val saveMutex: Mutex = Mutex()
    var autosaveJob: Job? = null
    var error by mutableStateOf(errorIn)
    private var text_ by mutableStateOf(textIn)
    private var savedText_ by mutableStateOf(savedTextIn)
    var title by mutableStateOf(recomputeTitle())

    val isModified: Boolean
        get() = text_ != savedText_

    private fun recomputeTitle() = (file.name ?: "Untitled") + if (isModified) "*" else ""

    fun setSavedText(savedTextIn: String, scope: CoroutineScope) {
        this.savedText_ = savedTextIn
        scope.launch(Dispatchers.Default) {
            val newTitle = recomputeTitle()
            withContext(Dispatchers.Swing) {
                title = newTitle
            }
        }
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
        private var nextTabId = 0
        private val nextTabIdMutex = Mutex()

        suspend fun createNew(file: File, initText: String, errorIn: String?): ObservableEditorTab{
            val newId = nextTabIdMutex.withLock {
                nextTabId++
            }
            return ObservableEditorTab(
                id = newId,
                file = file,
                textIn = initText,
                savedTextIn = initText,
                errorIn = errorIn
            )
        }
    }
}