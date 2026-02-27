package non.era.aisdme.ui.project

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import non.era.aisdme.configs.dataclass.child.TreeStateConfig
import non.era.aisdme.configs.dataclass.child.TreeStateElement
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.ProjectNode
import non.era.aisdme.utils.Log
import java.io.File

class ProjectTreeState {
    private val _nodes = mutableStateListOf<ProjectNode>()
    fun getNodes() = _nodes

    private val rootMutex = Mutex()
    private var rootProject: File? = null

    fun getSelectedNodes(): List<ProjectNode> {
        return ProjectTreeHelper.findSelectedNodes(_nodes)
    }

    fun expandSelected() {
        fun exp(node: DirNode) {
            node.expanded = true
            node.children.forEach {
                if (it is DirNode) exp(it)
            }
        }
        ProjectTreeHelper.findSelectedNodes(_nodes).forEach { node->
            if(node is DirNode) exp(node)
        }
    }

    fun collapseAll() {
        fun col(node: DirNode) {
            node.expanded = false
            node.children.forEach {
                if (it is DirNode) col(it)
            }
        }
        _nodes.forEach { node->
            if(node is DirNode) col(node)
        }
    }

    fun getCashForConfig() : TreeStateConfig {
        val line = ProjectListLineHelper.buildLinearNodes(_nodes).associate { node ->
            val isDirNode = node is DirNode
            val expanded = isDirNode && (node as DirNode).expanded
            val path: String = node.getFile().path
            path to TreeStateElement(isDirNode, expanded)
        }
        return TreeStateConfig(line)
    }

    fun reselectNode(node: ProjectNode) {
        getSelectedNodes().forEach { node ->
            if (node.isSelected()) node.setSelected(false)
        }
        node.setSelected(true)
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

    suspend fun reselectRootProject(config: TreeStateConfig?,root: File) =
        rootMutex.withLock {
            rootProject = root
            val newTree = ProjectTreeHelper.buildProjectTree(config, root)
            withContext(Dispatchers.Swing) {
                _nodes.clear()
                _nodes.addAll(newTree)
            }
        }

    //из контекстного меню обновление
    suspend fun reloadDirFromDisk(config: TreeStateConfig?) {
        getSelectedNodes().forEach { dirNode ->
            if(dirNode is DirNode) {
                val elements = ProjectTreeHelper.buildProjectTree(config, dirNode.getFile(), dirNode.depth + 1)
                withContext(Dispatchers.Swing) {
                    dirNode.children.clear()
                    dirNode.children.addAll(elements)
                }
            }
        }
    }

    suspend fun rebuildDirDiff(deer: File, notFoundErr: () -> Unit) =
        withContext(Dispatchers.Swing) {
            Log.d("rebuildDirDiff: ${deer.path}")
           ProjectTreeHelper.findDirNode(_nodes, deer)?.let { //todo файл в корне не ищется
               ProjectTreeHelper.rebuildDirDiff(it)
           } ?: run {
               notFoundErr()
               Log.e("findDirNode is null, reload all")
               val newTree = withContext(Dispatchers.IO) {
                   rootProject?.let {
                       ProjectTreeHelper.buildProjectTree(null, it)
                   } ?: run {
                       Log.e("rootProject in project tree is null")
                       emptyList()
                   }
               }
               withContext(Dispatchers.Swing) {
                   _nodes.clear()
                   _nodes.addAll(newTree)
               }
           }
        }
}