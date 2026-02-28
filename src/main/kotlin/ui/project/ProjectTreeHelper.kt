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
}