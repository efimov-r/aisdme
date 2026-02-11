package non.era.aisdme.ui.editor

import non.era.aisdme.ui.UiController
import java.io.File

enum class Panels{
    LEFT, RIGHT
}

class EditorController(
    val autosaveDelayMs: Long = 2000
) : UiController() {
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

    override fun disposeAfter() {
        leftTabs.saveAll()
        rightTabs.saveAll()
    }
}