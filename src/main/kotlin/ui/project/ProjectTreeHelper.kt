package non.era.aisdme.ui.project

import non.era.aisdme.configs.dataclass.child.SnapshotTree
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.LinkNode
import non.era.aisdme.ui.project.data.ProjectNode
import non.era.aisdme.utils.Log
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object ProjectTreeHelper {

    private const val MAX_DEPTH = 100 //глубже — без детей, иначе StackOverflowError и дерево не строится даже после перезапуска

    fun buildProjectTree(
        config: SnapshotTree?,
        root: File,
        depth: Int = 0
    ): DirNode? {
        if (!root.exists()) return null

        return DirNode(
            file = root,
            depth = depth,
            children = buildChildren(config, root, depth + 1),
            expand = config?.treeState?.get(root.path)?.expanded ?: true
        )
    }

    private fun buildChildren(
        config: SnapshotTree?,
        root: File,
        depth: Int,
        expandAll: Boolean = false,
    ): List<ProjectNode> {
        if (!root.exists()) return mutableListOf()
        if (depth > MAX_DEPTH) {
            Log.e("Слишком глубокая вложенность, дальше не читаю: $root")
            return mutableListOf()
        }

        return root.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?.map { f ->
                when {
                    Files.isSymbolicLink(f.toPath()) -> //раньше isDirectory: ссылка на родителя зацикливает обход
                        LinkNode(f, depth)

                    f.isDirectory -> {
                        val expand = expandAll || config?.treeState?.get(f.path)?.expanded ?: false
                        DirNode( //свёрнутая папка не читается: её содержимое — при раскрытии
                            file = f,
                            depth = depth,
                            children = if (expand) buildChildren(config, f, depth + 1, expandAll) else emptyList(),
                            expand = expand,
                            loaded = expand,
                        )
                    }

                    else ->
                        FileNode(f, depth)
                }
            } ?: emptyList()
    }

    fun findDirNode(
        nodes: List<ProjectNode>,
        dir: File
    ): DirNode? {
        for (node in nodes) {
            if (node is DirNode) {
                if(node.getFile() == dir) return node
                findDirNode(node.children, dir)?.let { return it }
            }
        }
        return null
    }

    fun findNode(
        nodes: List<ProjectNode>,
        file: File
    ): ProjectNode? {
        for (node in nodes) {
            if(node.getFile() == file) return node
            if (node is DirNode) {
                findNode(node.children, file)?.let { return it }
            }
        }
        return null
    }

    fun findSelectedNodes(nodes: List<ProjectNode>, selection: TreeSelection): List<ProjectNode> {
        if(selection.isEmpty()) return emptyList()
        val result: MutableList<ProjectNode> = mutableListOf()
        for (node in nodes) {
            if(selection.isSelected(node)) result.add(node)
            if (node is DirNode) result.addAll(findSelectedNodes(node.children, selection))
        }
        return result
    }

    /** Диск, не на ui-потоке. expandAll — всё поддерево раскрытым, иначе один уровень: вложенные папки не читаются */
    fun readChildren(node: DirNode, expandAll: Boolean = false) =
        buildChildren(null, node.getFile(), node.depth + 1, expandAll)

    /** Без диска, на ui-потоке. deep — и в прочитанные вложенные папки, иначе только этот уровень */
    fun applyDirDiff(node: DirNode, fresh: List<ProjectNode>, deep: Boolean = false) {
        val old = node.children.associateBy { it.getFilePath() }

        val result = mutableListOf<ProjectNode>()

        for (newNode in fresh) {
            val oldNode = old[newNode.getFilePath()]
            when {
                oldNode == null -> result += newNode

                oldNode::class != newNode::class ->  result += newNode

                oldNode is DirNode && newNode is DirNode -> {// сохраняем объект директории
                    if (deep && newNode.loaded) applyDirDiff(oldNode, newNode.children, deep = true)
                    result += oldNode
                }

                oldNode is FileNode && newNode is FileNode -> result += oldNode

                else -> result += newNode
            }
        }

        node.children.apply {
            clear()
            addAll(result)
        }
        node.loaded = true
    }

    fun loadedDirs(node: DirNode): List<DirNode> = //папка и прочитанные вложенные: их содержимое есть в дереве
        if (!node.loaded) emptyList()
        else listOf(node) + node.children.filterIsInstance<DirNode>().flatMap { loadedDirs(it) }

    fun selectFileInTree(
        nodes: List<ProjectNode>,
        target: File,
        selection: TreeSelection,
    ): Boolean {
        val path = mutableListOf<ProjectNode>()

        if ( !nodes.any { findPath(it, target.toPath(), path) } ) return false

        path.forEach { if (it is DirNode) it.expanded = true }
        selection.clear()
        selection.set(path.last(), true)
        return true
    }

    private fun findPath(
        node: ProjectNode,
        target: Path,
        path: MutableList<ProjectNode>
    ): Boolean {
        path.add(node)

        if (node.getFilePath() == target) {
            return true
        }

        if (node is DirNode) {
            for (child in node.children) {
                if (findPath(child, target, path)) {
                    return true
                }
            }
        }

        path.removeLast()
        return false
    }
}