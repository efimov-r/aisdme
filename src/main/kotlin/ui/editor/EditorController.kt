package non.era.aisdme.ui.editor

import java.io.File

class EditorController(
    autosaveDelayMs: Long = 2000
) {
    private var files: MutableMap<Int, ObservableOpenedFile> = mutableMapOf()

    val leftTabs = HalfEditorTabs(files, autosaveDelayMs)

    fun openFile(file: File) =
        leftTabs.openFile(file)

    fun saveAllAndDispose() {
        leftTabs.saveAllAndDispose()
    }
}