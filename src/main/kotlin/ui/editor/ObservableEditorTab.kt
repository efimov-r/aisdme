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
import kotlinx.coroutines.withContext
import java.io.File

class ObservableEditorTab(
    val id: Int,
    val file: File?,
    textIn: String,
    savedTextIn: String,
    errorIn: String? = null,
    val saveMutex: Mutex = Mutex(),
    var autosaveJob: Job? = null,
) {
    var error by mutableStateOf(errorIn)
    var text by mutableStateOf(textIn)
        private set
    private var savedText by mutableStateOf(savedTextIn)
    var title by mutableStateOf(recomputeTitle())

    val isModified: Boolean
        get() = text != savedText

    private fun recomputeTitle() = (file?.name ?: "Untitled") + if (isModified) "*" else ""

    fun setSavedTextWithUpTitle(savedTextIn: String, scope: CoroutineScope) {
        this.savedText = savedTextIn
        scope.launch(Dispatchers.Default) {
            val newTitle = recomputeTitle()
            withContext(Dispatchers.Swing) {
                title = newTitle
            }
        }
    }

    fun setTextWithUpTitle(textIn: String, scope: CoroutineScope) {
        this.text = textIn
        scope.launch(Dispatchers.Default) {
            val newTitle = recomputeTitle()
            withContext(Dispatchers.Swing) {
                title = newTitle
            }
        }
    }
}