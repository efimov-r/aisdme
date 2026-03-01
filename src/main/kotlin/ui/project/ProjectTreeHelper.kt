package non.era.aisdme.ui.project

import non.era.aisdme.configs.dataclass.child.SnapshotTree
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.LinkNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object ProjectTreeHelper {

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
        depth: Int
    ): List<ProjectNode> {
        if (!root.exists()) return mutableListOf()

        return root.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?.map { f ->
                when {
                    f.isDirectory -> DirNode(
                        file = f,
                        depth = depth,
                        children = buildChildren(config, f, depth + 1),
                        expand = config?.treeState?.get(f.path)?.expanded ?: false
                    )

                    Files.isSymbolicLink(f.toPath()) ->
                        LinkNode(f, depth)

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

    fun findSelectedNodes(nodes: List<ProjectNode>): List<ProjectNode> {
        val result: MutableList<ProjectNode> = mutableListOf()
        for (node in nodes) {
            if(node.isSelected()) result.add(node)
            if (node is DirNode) result.addAll(findSelectedNodes(node.children))
        }
        return result
    }

    fun rebuildDirDiff(node: DirNode) {
        val old = node.children.associateBy { it.getFilePath() }
        val fresh = buildChildren(null, node.getFile(), node.depth + 1)

        val result = mutableListOf<ProjectNode>()

        for (newNode in fresh) {
            val oldNode = old[newNode.getFilePath()]
            if (oldNode is DirNode && newNode is DirNode) {
                // сохраняем старый узел, обновляем детей
                rebuildDirDiff(oldNode)
                result += oldNode
            } else {
                result += newNode
            }
        }

        node.children.clear()
        node.children.addAll(result)
    }

    fun selectFileInTree(
        nodes: List<ProjectNode>,
        target: File
    ): Boolean {
        val path = mutableListOf<ProjectNode>()

        if ( !nodes.any { findPath(it, target.toPath(), path) } ) return false

        path.forEach { if (it is DirNode) it.expanded = true }
        ProjectTreeHelper.findSelectedNodes(nodes).forEach { it.setSelected(false) }
        path.last().setSelected(true)
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