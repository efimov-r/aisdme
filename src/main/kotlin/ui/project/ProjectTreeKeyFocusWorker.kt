package non.era.aisdme.ui.project

import androidx.compose.ui.input.key.KeyEvent
import non.era.aisdme.AppController
import non.era.aisdme.Hotkeys
import non.era.aisdme.files.FileFMOpener
import non.era.aisdme.ui.editor.Panels

fun treeKeyFocusWork(event: KeyEvent, app: AppController): Boolean {
    val selected = app.getProjectState().getSelectedNode()
    when {
        Hotkeys.COPY.ifDu(event) -> {
            if(selected is FileNode || selected is DirNode) {
                app.copyToClipboard(listOf(selected.file.toPath()), false)
            }
            return true
        }

        Hotkeys.PASTE.ifDu(event) -> {
            if(selected is DirNode) {
                app.pasteFromClipboard(selected.file.toPath())
            }
            return true
        }

        //todo вырезка-вставка папки падает в ошибку
        Hotkeys.CUT.ifDu(event) -> {
            if(selected is FileNode || selected is DirNode) {
                app.cutNode(selected)
            }
            return true
        }

        //todo удаленная директория торчит в списке (не обновляется)
        Hotkeys.DELETE.ifDu(event) -> { //todo dialog
            if (selected is FileNode || selected is LinkNode) {
                app.delete(selected.file.toPath())
            }
            if(selected is DirNode) {
                app.deleteRecursive(selected.file.toPath())
            }
            return true
        }

        Hotkeys.OPEN_DOLPHIN.ifDu(event) -> {
            selected?.let { FileFMOpener.openFileInFileManager(it.file) }
            return true
        }

        Hotkeys.ENTER.ifDu(event) -> {
            if(selected is DirNode) {
                selected.openOrClose()
            }
            return true
        }

        Hotkeys.LEFT.ifDu(event) -> {
            if(selected is FileNode) {
                app.openFileInEditor(selected.file, Panels.LEFT)
            }
            return true
        }

        Hotkeys.RIGHT.ifDu(event) -> {
            if(selected is FileNode) {
                app.openFileInEditor(selected.file, Panels.RIGHT)
            }
            return true
        }

        Hotkeys.TOP.ifDu(event) -> {
            app.reselectNode(ProjectListBuilder.build(app).lastOf(selected))
            return true
        }

        Hotkeys.BOTTOM.ifDu(event) -> {
            app.reselectNode(ProjectListBuilder.build(app).nextOf(selected))
            return true
        }
        else -> return false
    }


}

object ProjectListBuilder {
    private var result: MutableList<ProjectNode> = mutableListOf()

    private fun dirBuild(node: DirNode) {
        result.add(node)
        if (node.expanded.value) {
            node.children.forEach { child ->
                nodeBuild(child)
            }
        }
    }

    private fun nodeBuild(node: ProjectNode) {
        when (node) {
            is DirNode -> dirBuild(node)
            is FileNode -> result.add(node)
            is LinkNode -> result.add(node)
        }
    }

    fun build(app: AppController) : ProjectListBuilder {
        result = mutableListOf()
        val nodes = app.getProjectState().getNodes()
        nodes.forEach { node ->
            nodeBuild(node)
        }
        return this
    }

    fun nextOf(node: ProjectNode?) : ProjectNode?  {
        if(node == null) return null
        val i = result.indexOfFirst { it.file == node.file }
        return result.getOrNull(i + 1) ?: node
    }

    fun lastOf(node: ProjectNode?) : ProjectNode?  {
        if(node == null) return null
        val i = result.indexOfFirst { it.file == node.file }
        return result.getOrNull(i - 1) ?: node
    }
}