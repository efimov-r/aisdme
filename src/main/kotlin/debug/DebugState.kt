package non.era.aisdme.debug

import non.era.aisdme.di.AppController
import non.era.aisdme.ui.editor.HalfEditorTabs
import non.era.aisdme.ui.editor.halfviewmodel.TabCashFile
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText
import non.era.aisdme.ui.project.ProjectListLineHelper
import non.era.aisdme.ui.project.ProjectTreeState
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.ProjectNode
import kotlin.math.roundToInt

/** Состояние приложения текстом: проверять результат без скриншота. Вызывать на ui-потоке. */
object DebugState {
    fun dump(app: AppController): String = buildString {
        appendLine("theme: ${if (app.themeController.isDarkTheme) "dark" else "light"}")
        appendLine("tree (* — выделен, @x,y — точка клика в пикселях скриншота):")
        val tree = app.projectController.treeState
        ProjectListLineHelper.buildLinearExpandedNodes(tree.getNodes()).forEach { appendLine(treeLine(tree, it)) }
        appendLine("left: ${halfLine(app, app.editorController.leftTabs)}")
        appendLine("right: ${halfLine(app, app.editorController.rightTabs)}")
    }

    /** Полный текст активной вкладки половины: `left` (по умолчанию) или `right` */
    fun text(app: AppController, half: String): String {
        val tabs = if (half == "right") app.editorController.rightTabs else app.editorController.leftTabs
        val tab = tabs.activeTabId?.let { app.editorController.getTab(it) }
        return (tab as? TabCashFileText)?.textFieldValue?.text ?: error("активная вкладка не текстовая")
    }

    /** Точка узла дерева в пикселях скриншота. Имя — как в `state`; для одинаковых имён — путь от корня через `/` */
    fun nodePoint(app: AppController, name: String): Pair<Int, Int> {
        val tree = app.projectController.treeState
        val nodes = ProjectListLineHelper.buildLinearExpandedNodes(tree.getNodes())
        val root = nodes.firstOrNull()?.getFilePath() ?: error("дерево пусто")
        fun path(n: ProjectNode) = //от корня через /; у самого корня — его имя
            root.relativize(n.getFilePath()).toString().replace('\\', '/').ifEmpty { n.getFileName() }

        val found = nodes.filter { path(it) == name }.ifEmpty { nodes.filter { path(it).endsWith(name) } }
        val node = found.singleOrNull() ?: error("узлов с именем «$name»: ${found.size} ${found.map(::path)}")
        val anchor = tree.menuAnchor(node) ?: error("у «$name» нет позиции на экране")
        return anchor.x.roundToInt() to anchor.y.roundToInt()
    }

    private fun treeLine(tree: ProjectTreeState, node: ProjectNode): String {
        val dir = (node as? DirNode)?.let { "/ " + if (it.expanded) "[-]" else "[+]" } ?: ""
        val selected = if (tree.selection.isSelected(node)) " *" else ""
        val anchor = tree.menuAnchor(node)?.let { " @${it.x.roundToInt()},${it.y.roundToInt()}" } ?: ""
        return "  ".repeat(node.depth + 1) + node.getFileName() + dir + selected + anchor
    }

    private fun halfLine(app: AppController, half: HalfEditorTabs): String {
        if (half.listTabs.isEmpty()) return "-"
        return half.listTabs.joinToString(", ") { id ->
            val tab = app.editorController.getTab(id)
            val status = (tab as? TabCashFile)?.writeStatus?.text?.let { " ($it)" } ?: ""
            (if (id == half.activeTabId) "*" else "") + (tab?.name ?: "?$id") + status
        }
    }
}