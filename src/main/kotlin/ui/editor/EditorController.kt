package non.era.aisdme.ui.editor

import java.io.File

enum class Panels{
    LEFT, RIGHT
}

class EditorController(
    autosaveDelayMs: Long = 2000
) {
    private var files: MutableMap<Int, ObservableOpenedFile> = mutableMapOf()

    val leftTabs = HalfEditorTabs(files, autosaveDelayMs)
    val rightTabs = HalfEditorTabs(files, autosaveDelayMs)

    fun openFile(file: File, pane: Panels = Panels.LEFT) {
        when(pane){
            Panels.RIGHT -> rightTabs.openFile(file)
            Panels.LEFT -> leftTabs.openFile(file)
        }
    }

    fun saveAllAndDispose() {
        leftTabs.saveAllAndDispose()
        rightTabs.saveAllAndDispose()
    }
}