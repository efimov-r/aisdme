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

    var onDirsLoaded: (List<File>) -> Unit = {} //прочитанные с диска папки: watcher на Linux следит только за ними

    private val rootMutex = Mutex()
    private val diskMutex = Mutex() //чтение папок по очереди: старое чтение не применится поверх нового
    @Volatile private var rootProject: File? = null

    fun getSelectedNodes(): List<ProjectNode> {
        return ProjectTreeHelper.findSelectedNodes(_nodes) //обход только прочитанных папок
    }

    suspend fun showNode(file: File) {
        val root = rootProject ?: return
        val dirs = generateSequence(file.parentFile) { it.parentFile } //папки на пути, от корня
            .takeWhile { it.toPath().startsWith(root.toPath()) }
            .toList()
            .asReversed()
        for (dir in dirs) {
            val node = withContext(Dispatchers.Swing) { ProjectTreeHelper.findDirNode(_nodes, dir) } ?: break
            if (!node.loaded) loadChildren(node)
        }
        withContext(Dispatchers.Swing) { inFocus = ProjectTreeHelper.selectFileInTree(_nodes, file) }
    }

    suspend fun loadChildren(node: DirNode) = diskMutex.withLock {
        val fresh = withContext(Dispatchers.IO) { ProjectTreeHelper.readChildren(node) }
        withContext(Dispatchers.Swing) { ProjectTreeHelper.applyDirDiff(node, fresh) }
        onDirsLoaded(listOf(node.getFile()))
    }

    suspend fun expandSelected() = diskMutex.withLock { //всё поддерево: читается с диска целиком
        val dirs = withContext(Dispatchers.Swing) { getSelectedNodes().filterIsInstance<DirNode>() }
        dirs.forEach { dir ->
            val fresh = withContext(Dispatchers.IO) { ProjectTreeHelper.readChildren(dir, expandAll = true) }
            val loaded = withContext(Dispatchers.Swing) {
                ProjectTreeHelper.applyDirDiff(dir, fresh, deep = true)
                expandAll(dir)
                ProjectTreeHelper.loadedDirs(dir).map { it.getFile() }
            }
            onDirsLoaded(loaded)
        }
    }

    private fun expandAll(node: DirNode) {
        node.expanded = true
        node.children.forEach { if (it is DirNode) expandAll(it) }
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

    fun getCashForConfig() = SnapshotTree( //только раскрытые папки и корень: остальные по умолчанию свёрнуты
        ProjectListLineHelper.buildLinearNodes(_nodes)
            .filterIsInstance<DirNode>()
            .filter { it.expanded || it.depth == 0 }
            .associate { it.getFile().path to TreeStateElement(true, it.expanded) }
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
            val newTree = withContext(Dispatchers.IO) { ProjectTreeHelper.buildProjectTree(config, root) }
            setTree(newTree)
        }

    private suspend fun setTree(tree: DirNode?) {
        withContext(Dispatchers.Swing) {
            _nodes.clear()
            tree?.let { _nodes.add(it) }
        }
        tree?.let { onDirsLoaded(ProjectTreeHelper.loadedDirs(it).map { d -> d.getFile() }) }
    }

    //из контекстного меню обновление
    suspend fun reloadSelectedDirFromDisk() {
        val dirs = withContext(Dispatchers.Swing) {
            getSelectedNodes().filterIsInstance<DirNode>().map { it.getFile() }
        }
        dirs.forEach { dir ->
            rebuildDirDiff(dir, {})
        }
    }

    private fun findNearestDirNode(dir: File): DirNode? =
        generateSequence(dir) { it.parentFile }.firstNotNullOfOrNull { ProjectTreeHelper.findDirNode(_nodes, it) }

    suspend fun rebuildDirDiff(deer: File, notFoundErr: () -> Unit): Unit = diskMutex.withLock {
        Log.d("rebuildDirDiff: ${deer.path}")
        val node = withContext(Dispatchers.Swing) { findNearestDirNode(deer) } //нет в дереве — ближайший предок
        if (node != null) {
            if (node.loaded) refresh(node) //не прочитанная папка прочитается при раскрытии
            return@withLock
        }

        notFoundErr()
        Log.e("findDirNode is null, reload all")
        val config = withContext(Dispatchers.Swing) { getCashForConfig() } //раскрытые папки не сворачиваются
        val newTree = withContext(Dispatchers.IO) {
            rootProject?.let {
                ProjectTreeHelper.buildProjectTree(config, it)
            } ?: run {
                Log.e("rootProject in project tree is null")
                null
            }
        }
        setTree(newTree)
    }

    private suspend fun refresh(node: DirNode) { //и прочитанные вложенные: после OVERFLOW изменения где угодно
        val dirs = withContext(Dispatchers.Swing) { ProjectTreeHelper.loadedDirs(node) }
        val fresh = withContext(Dispatchers.IO) { dirs.map { it to ProjectTreeHelper.readChildren(it) } }
        withContext(Dispatchers.Swing) {
            fresh.forEach { (dir, children) -> ProjectTreeHelper.applyDirDiff(dir, children) }
        }
    }
}