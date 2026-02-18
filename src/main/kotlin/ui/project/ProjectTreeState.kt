package non.era.aisdme.ui.project

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import non.era.aisdme.utils.Log
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.LinkNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File
import java.nio.file.Files
import kotlin.collections.plusAssign

class ProjectTreeState {
    private val _nodes = mutableStateListOf<ProjectNode>()
    fun getNodes() = _nodes

    private val rootMutex = Mutex()
    private var rootProject: File? = null

    fun getSelectedNodes(): List<ProjectNode> {
        return findSelectedNodes(_nodes)
    }

    fun reselectNode(node: ProjectNode) {
        getSelectedNodes().forEach { node ->
            if (node.isSelected()) node.setSelected(false)
        }
        node.setSelected(true)
    }

    private fun findSelectedNodes(nodes: List<ProjectNode>): List<ProjectNode> {
        val result: MutableList<ProjectNode> = mutableListOf()
        for (node in nodes) {
            if(node.isSelected()) result.add(node)
            if (node is DirNode) result.addAll(findSelectedNodes(node.children))
        }
        return result
    }

    //пометка что вырезано
    fun cutNoteMarkRecursive(node: ProjectNode) {
        fun cutNodesUnmark(nodes: List<ProjectNode>): Unit =
            nodes.filterIsInstance<DirNode>().forEach { node ->
                if (node.isCutted) node.isCutted = false
                cutNodesUnmark(node.children)
            }
        fun cutNodeMark(node: ProjectNode) {
            node.isCutted = true
            if(node is DirNode)
                node.children.forEach { cutNodeMark(it) }
        }

        cutNodesUnmark(_nodes)
        cutNodeMark(node)
    }

    suspend fun reselectRootProject(root: File) =
        rootMutex.withLock {
            rootProject = root
            val newTree = buildProjectTree(rootProject!!)
            withContext(Dispatchers.Swing) {
                _nodes.clear()
                _nodes.addAll(newTree)
            }
        }

    //из контекстного меню обновление
    fun reloadDirFromDisk() {
        getSelectedNodes().forEach { dirNode ->
            if(dirNode is DirNode) {
                dirNode.children.clear()
                dirNode.children.addAll(buildProjectTree(dirNode.getFile(), dirNode.depth + 1))
            }
        }
    }

    suspend fun rebuildDirDiff(deer: File) =
        withContext(Dispatchers.Swing) {
            val finded = findDirParentNode(_nodes, deer)
            if (finded != null) {
                rebuildDirDiff(finded)
            } else {
                Log.e("findDirNode is null, reload all")
                withContext(Dispatchers.IO) {
                    rootProject?.let {
                        reselectRootProject(it)
                    } ?: Log.e("rootProject in project tree is null")
                }
            }
        }

    private fun rebuildDirDiff(node: DirNode) {
        val old = node.children.associateBy { it.getFilePath() }
        val fresh = buildProjectTree(node.getFile(), node.depth + 1)

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

    private fun findDirParentNode(
        nodes: List<ProjectNode>,
        dir: File
    ): DirNode? {
        for (node in nodes) {
            if (node is DirNode) {
                node.children.forEach { ch->
                    if (ch.getFile() == dir) return node
                }
                findDirParentNode(node.children, dir)?.let { return it }
            }
        }
        return null
    }


    private fun buildProjectTree(
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
                        children = buildProjectTree(f, depth + 1)
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
}