package non.era.aisdme

import androidx.compose.runtime.staticCompositionLocalOf
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.editor.EditorHalfState
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.project.ProjectState
import java.io.File

val LocalAppController = staticCompositionLocalOf<AppController> {
    error("AppController not provided")
}

class AppController(
    val editor: EditorController,
    val project: ProjectController
) {
    fun openFile(file: File, panel: Panels) {
        editor.openFile(file, panel)
    }

    fun rightToLeftHalf() = editor.rightToLeftHalf()

    fun getEditorHalfLeftController() : EditorHalfState = editor.leftTabs
    fun getEditorHalfRightController() : EditorHalfState = editor.rightTabs
    fun getProjectState() : ProjectState = project
}