package non.era.aisdme.ui.project

import non.era.aisdme.configs.dataclass.child.SnapshotTree
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.LinkNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File
import java.nio.file.Files

object ProjectTreeHelper {

    fun buildProjectTree(
        config: SnapshotTree?,
        root: File,
        depth: Int = 0
    ): List<ProjectNode> {
        if (!root.exists()) return emptyList()

        return root.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?.map { f ->
                if (f.isDirectory) {
                    DirNode(
                        file = f,
                        depth = depth,
                        children = buildProjectTree(config, f, depth + 1),
                        expand = config?.treeState?.get(f.path)?.expanded ?: false
                    )
                } else if(Files.isSymbolicLink(f.toPath())) {
                    LinkNode(
                        file = f,
                        depth = depth
                    )
                } else {
                    FileNode(
                        file = f,
                        depth = depth
                    )
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
        val fresh = buildProjectTree(null, node.getFile(), node.depth + 1)

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
}