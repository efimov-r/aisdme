package non.era.aisdme.ui.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import non.era.aisdme.configs.dataclass.child.SnapshotTree
import non.era.aisdme.configs.dataclass.child.TreeStateElement
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.ProjectNode
import non.era.aisdme.utils.Log
import java.io.File

class ProjectTreeState {
    private val _nodes = mutableStateListOf<ProjectNode>()
    fun getNodes() = _nodes

    var inFocus by mutableStateOf(false)

    private val rootMutex = Mutex()
    private var rootProject: File? = null

    fun getSelectedNodes(): List<ProjectNode> {
        return ProjectTreeHelper.findSelectedNodes(_nodes)
    }

    fun showNode(file: File) {
        inFocus = ProjectTreeHelper.selectFileInTree(_nodes, file)
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
            node.setSelected(false)
            node.children.forEach {
                it.setSelected(false)
                if (it is DirNode) col(it)
            }
        }
        _nodes.forEach { node->
            if(node is DirNode) col(node)
        }
    }

    fun getCashForConfig() = SnapshotTree(
        ProjectListLineHelper.buildLinearNodes(_nodes).associate { node ->
            val isDirNode = node is DirNode
            val expanded = isDirNode && (node as DirNode).expanded
            val path: String = node.getFile().path
            path to TreeStateElement(isDirNode, expanded)
        }
    )


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

    suspend fun reselectRootProject(config: SnapshotTree?, root: File) =
        rootMutex.withLock {
            rootProject = root
            val newTree = ProjectTreeHelper.buildProjectTree(config, root)
            withContext(Dispatchers.Swing) {
                _nodes.clear()
                newTree?.let { _nodes.add(it) }
            }
        }

    //из контекстного меню обновление
    suspend fun reloadSelectedDirFromDisk() {
        getSelectedNodes().forEach { dirNode ->
            rebuildDirDiff(dirNode.getFile(), {})
        }
    }

    fun findNodes(file: File) = ProjectTreeHelper.findNode(_nodes, file)

    suspend fun rebuildDirDiff(deer: File, notFoundErr: () -> Unit) =
        withContext(Dispatchers.Swing) {
            Log.d("rebuildDirDiff: ${deer.path}")
           ProjectTreeHelper.findDirNode(_nodes, deer)?.let {
               ProjectTreeHelper.rebuildDirDiff(it)
           } ?: run {
               notFoundErr()
               Log.e("findDirNode is null, reload all")
               val newTree = withContext(Dispatchers.IO) {
                   rootProject?.let {
                       ProjectTreeHelper.buildProjectTree(null, it)
                   } ?: run {
                       Log.e("rootProject in project tree is null")
                       null
                   }
               }
               withContext(Dispatchers.Swing) {
                   _nodes.clear()
                   newTree?.let { _nodes.add(it) }
               }
           }
        }
}