package non.era.aisdme.ui.project

import androidx.compose.ui.input.key.KeyEvent
import non.era.aisdme.AppController
import non.era.aisdme.Hotkeys
import non.era.aisdme.ui.editor.Panels

fun treeKeyFocusWork(
    event: KeyEvent,
    app: AppController,
    showDelDialog: ()->Unit,
): Boolean {
    when {
        Hotkeys.COPY.ifDu(event) -> {
            app.copySelectedToClipboard(false)
            return true
        }

        Hotkeys.CUT.ifDu(event) -> {
            app.copySelectedToClipboard(true)
            return true
        }

        Hotkeys.PASTE.ifDu(event) -> {
            app.pasteToSelectedNode()
            return true
        }

        Hotkeys.DELETE.ifDu(event) -> {
            showDelDialog()
            return true
        }

        Hotkeys.OPEN_DOLPHIN.ifDu(event) -> {
            app.openSelectedInFileManager()
            return true
        }

        Hotkeys.ENTER.ifDu(event) -> {
            val selected = app.getSelectedNodes()
            if(selected.size == 1 && selected[0] is DirNode) {
                (selected[0] as DirNode).openOrClose()
                return true
            }
            if(selected.size == 1 && selected[0] is FileNode) {
                app.openSelectedNodesInEditor(Panels.LEFT)
            }
            return true
        }

        Hotkeys.LEFT.ifDu(event) -> {
            app.openSelectedNodesInEditor(Panels.LEFT)
            return true
        }

        Hotkeys.RIGHT.ifDu(event) -> {
            app.openSelectedNodesInEditor(Panels.RIGHT)
            return true
        }

        Hotkeys.TOP.ifDu(event) -> {
            val selected = app.getSelectedNodes()
            if(selected.size == 1) {
                ProjectListBuilder.build(app).lastOf(selected[0])?.let {
                    app.reselectNode(it)
                }

            }
            return true
        }

        Hotkeys.BOTTOM.ifDu(event) -> {
            val selected = app.getSelectedNodes()
            if(selected.size == 1) {
                ProjectListBuilder.build(app).nextOf(selected[0])?.let {
                    app.reselectNode(it)
                }
            }
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
        val nodes = app.getNodes()
        nodes.forEach { node ->
            nodeBuild(node)
        }
        return this
    }

    fun nextOf(node: ProjectNode?) : ProjectNode?  {
        if(node == null) return null
        val i = result.indexOfFirst { it.equalsOf(node) }
        return result.getOrNull(i + 1) ?: node
    }

    fun lastOf(node: ProjectNode?) : ProjectNode?  {
        if(node == null) return null
        val i = result.indexOfFirst { it.equalsOf(node) }
        return result.getOrNull(i - 1) ?: node
    }
}