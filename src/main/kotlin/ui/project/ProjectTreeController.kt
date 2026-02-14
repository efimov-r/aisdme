package non.era.aisdme.ui.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import non.era.aisdme.Log
import non.era.aisdme.ui.UiController
import java.io.File
import java.nio.file.Files

class ProjectTreeController: UiController(), TreeState {
    private val _nodes = mutableStateListOf<ProjectNode>()
    override fun getNodes() = _nodes

    private val rootMutex = Mutex()
    private var rootProject: File? = null

    override fun getSelectedNodes(): List<ProjectNode> {
        return findSelectedNodes(_nodes)
    }

    //клик по элементу
    override fun reselectNode(node: ProjectNode) {
        getSelectedNodes().forEach { node ->
            node.setSelected(false)
        }
        node.setSelected(true)
    }

    override fun addSelectNode(node: ProjectNode) = node.setSelected(true)

    private fun findSelectedNodes(nodes: List<ProjectNode>): List<ProjectNode> {
        val result: MutableList<ProjectNode> = mutableListOf()
        for (node in nodes) {
            if(node.isSelected()) {
                result.add(node)
            }
            if (node is DirNode) {
                result.addAll(findSelectedNodes(node.children))
            }
        }
        return result
    }

    //пометка что вырезано
    fun cutNoteMarkRecursive(node: ProjectNode) {
        fun cutNodesUnmark(nodes: List<ProjectNode>) {
            for (node in nodes) {
                if (node is DirNode) {
                    if (node.isCutted) node.isCutted = false
                    cutNodesUnmark(node.children)
                }
            }
        }
        fun cutNodeMark(node: ProjectNode) {
            node.isCutted = true
            if(node is DirNode) {
                node.children.forEach { cutNodeMark(it) }
            }
        }

        cutNodesUnmark(_nodes)
        cutNodeMark(node)
    }

    //todo пока только при старте
    suspend fun setRootProject(root: File) =
        rootMutex.withLock {
            rootProject = root
        }

    suspend fun rebuildAllTree() {
        val newTree = rootMutex.withLock {
            if(rootProject == null) {
                withContext(Dispatchers.Swing) {
                    newGlobalError("Root project not found")
                }
                return@withLock emptyList()
            } else {
                return@withLock buildProjectTree(rootProject!!)
            }
        }
        withContext(Dispatchers.Swing) {
            _nodes.clear()
            _nodes.addAll(newTree)
        }
    }

    //из контекстного меню обновление
    fun reloadDirFromDisk() {
        getSelectedNodes().forEach { dirNode ->
            if(dirNode is DirNode) {
                Log.i("rebuild child from disk: ${dirNode.getFileString()}")
                dirNode.children.clear()
                dirNode.children.addAll(buildProjectTree(dirNode.getFile(), dirNode.depth + 1))
            }
        }
    }

    suspend fun rebuildDirDiff(deer: File) =
        withContext(Dispatchers.Swing) {
            Log.d("!rebuildDirDiff, long in ui")
            findDirParentNode(_nodes, deer)?.let { dirNode ->
                Log.i("rebuildDirDiff, pa ${dirNode.getFileString()}")
                rebuildDirDiff(dirNode)
            } ?: {
                Log.e("findDirNode is null, reload all")
                scope.launch {
                    rebuildAllTree()
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