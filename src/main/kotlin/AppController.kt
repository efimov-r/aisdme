package non.era.aisdme

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.files.FilesController
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.editor.EditorHalfState
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.project.TreeState
import java.io.File

val LocalAppController = staticCompositionLocalOf<AppController> {
    error("AppController not provided")
}

class AppController(
    private val editor: EditorController,
    private val project: ProjectController,
    private val filesController: FilesController,
) {
    private val scopeFlow = CoroutineScope(SupervisorJob() + Dispatchers.Swing)
    private val _errors = mutableStateListOf<String>() //todo перейти на класс
    val errors: List<String> get() = _errors

    init {
        scopeFlow.launch {
            filesController.errors.collect { error ->
                _errors += error
            }
        }
    }

    fun openFileInEditor(file: File, panel: Panels) = editor.openFile(file, panel)
    fun rightToLeftHalf() = editor.rightToLeftHalf()

    fun getEditorHalfLeftController() : EditorHalfState = editor.leftTabs
    fun getEditorHalfRightController() : EditorHalfState = editor.rightTabs
    fun getProjectState() : TreeState = project

    fun clearErrors() {
        _errors.clear()
    }

    fun dispose() {
        scopeFlow.cancel()
        editor.dispose()
        project.dispose()
        filesController.dispose()
    }
}