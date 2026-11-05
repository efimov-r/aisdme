package non.era.aisdme.ui.project

import non.era.aisdme.files.work.IFileController
import non.era.aisdme.platform.Platform
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.utils.Log

class TreeActions( //операции над выделенными узлами дерева: связывают дерево с редактором и файлами
    private val treeState: ProjectTreeState,
    private val editorController: EditorController,
    private val filesController: IFileController,
) {
    fun openSelectedInEditor(panel: Panels) = editorController.openNodes(treeState.getSelectedNodes(), panel)

    fun showSelectedInFileManager() = treeState.getSelectedNodes()
        .map { it.getFile() }
        .filter { it.exists() }
        .forEach { f ->
            runCatching { Platform.current.showInFileManager(f) }.onFailure { e -> Log.e(e) }
        }

    fun copySelectedToClipboard(cut: Boolean) {
        val selected = treeState.getSelectedNodes()
        if(cut) treeState.markCut(selected)
        filesController.copyToClipboard(selected.map { it.getFilePath() }, cut)
    }
}