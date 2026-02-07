package non.era.aisdme.ui.editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.File

enum class Panels{
    LEFT, RIGHT
}

class EditorController(
    val autosaveDelayMs: Long = 2000
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var files: MutableMap<Int, ObservableOpenedFile> = mutableMapOf()

    var leftTabs = HalfEditorTabs(files, autosaveDelayMs, scope)
    var rightTabs = HalfEditorTabs(files, autosaveDelayMs, scope)

    fun openFile(file: File, pane: Panels = Panels.LEFT) {
        when(pane){
            Panels.RIGHT -> rightTabs.openFile(file)
            Panels.LEFT -> leftTabs.openFile(file)
        }
    }

    fun rightToLeftHalf() {
        leftTabs.closeAllTabs()
        leftTabs = rightTabs
        rightTabs = HalfEditorTabs(files, autosaveDelayMs, scope)
    }

    fun saveAllAndDispose() {
        leftTabs.saveAll()
        rightTabs.saveAll()
        scope.cancel()
    }
}