package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

data class EditorState(
    val file: File?,
    val savedText: String,
    val text: String,
    val error: String? = null,
) {
    val isModified: Boolean
        get() = text != savedText
}

class EditorController(
    initialFile: File?,
    private val autosaveDelayMs: Long = 2000
) {
    private val saveMutex = Mutex()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var autosaveJob: Job? = null

    var state by mutableStateOf(
        runCatching {
            val text = initialFile?.takeIf { it.exists() }?.readText() ?: ""
            EditorState(
                file = initialFile,
                text = text,
                savedText = text
            )
        }.getOrElse {
            EditorState(
                file = initialFile,
                text = "",
                savedText = "",
                error = it.message
            )
        }
    )
        private set

    fun openFile(file: File) {
        state = runCatching {
            val t = if (file.exists()) file.readText() else ""
            EditorState(
                file = file,
                text = t,
                savedText = t
            )
        }.getOrElse {
            state.copy(error = it.message)
        }
    }

    fun updateText(newText: String) {
        state = state.copy(text = newText)

        scheduleAutosave()
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()

        autosaveJob = scope.launch {
            delay(autosaveDelayMs)

            if (!state.isModified) return@launch

            val result = saveMutex.withLock {
                runCatching {
                    val file = state.file ?: return@runCatching null
                    val text = state.text
                    file.writeText(text)
                    return@runCatching text
                }
            }

            withContext(Dispatchers.Main) {
                state = result.fold(
                    onSuccess = { savedText ->
                        if (savedText == null) state
                        else state.copy(savedText = savedText, error = null)
                    },
                    onFailure = {
                        state.copy(error = it.message)
                    }
                )
            }
        }
    }

    fun saveNow() {
        autosaveJob?.cancel()
        CoroutineScope(Dispatchers.IO).launch {
            saveMutex.withLock {
                val file = state.file ?: return@withLock

                state = runCatching {
                    file.writeText(state.text)
                    state.copy(savedText = state.text, error = null)
                }.getOrElse {
                    state.copy(error = it.message)
                }
            }
        }
    }


    fun dispose() {
        scope.cancel()
    }
}