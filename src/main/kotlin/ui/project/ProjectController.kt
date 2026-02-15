package non.era.aisdme.ui.project

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.AppEventBus
import non.era.aisdme.FileOpened
import non.era.aisdme.Log
import non.era.aisdme.ui.UiController
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*

enum class TypeClickNode{
    LMB, LMB_DOWN, DLMB, RMB, CTRL_LMB, SHIFT_LMB
}

class ProjectController(
    projectRoot: File,
    private val bus: AppEventBus
) : UiController() {
    val root = projectRoot
    private val watchService = FileSystems.getDefault().newWatchService()

    private val projectTree = ProjectTreeController()
    private val treeReloadWatcher = TreeReloadWatcher{ dir->
        projectTree.rebuildDirDiff(dir)
    }

    init {
        scope.launch {
            projectTree.setRootProject(root)
            projectTree.rebuildAllTree()
        }
        treeReloadWatcher.registerRecursively(root.toPath())
        treeReloadWatcher.watchLoop(scope)
        projectTree.subscriptionError { emitGlobalMessage(it) }
    }

    fun cutNoteMarkRecursive(node: ProjectNode) = projectTree.cutNoteMarkRecursive(node)

    fun reloadDeerFromDisk() = projectTree.reloadDirFromDisk()

    fun getNodes() = projectTree.getNodes()
    fun getSelectedNodes(): List<ProjectNode> = projectTree.getSelectedNodes()
    fun reselectNode(node: ProjectNode) = projectTree.reselectNode(node)
    fun addSelectNode(node: ProjectNode) = projectTree.addSelectNode(node)

    private var nodeClicked: ProjectNode? = null
    fun clickNode(node: ProjectNode, typeClick: TypeClickNode) {
        val selected = projectTree.getSelectedNodes()
        when (typeClick) {
            TypeClickNode.LMB -> {
                if(selected.size <= 1){
                    nodeClicked = node
                    projectTree.reselectNode(node)
                }
            }
            TypeClickNode.LMB_DOWN -> {
                if(selected.size > 1){
                    if(nodeClicked?.equalsOf(node) == true) {
                        nodeClicked = null
                    } else {
                        projectTree.reselectNode(node)
                    }
                }
            }
            TypeClickNode.DLMB -> {
                nodeClicked = node
                if(node is FileNode){
                    bus.publish(FileOpened(node.getFile()))
                } else if (node is DirNode) {
                    node.openOrClose()
                }
            }
            TypeClickNode.RMB -> {
                if(selected.size <= 1) {
                    projectTree.reselectNode(node)
                }
            }
            TypeClickNode.CTRL_LMB -> {
                nodeClicked = node
                projectTree.addSelectNode(node)
            }
            TypeClickNode.SHIFT_LMB -> {
                nodeClicked = node
                ProjectListBuilder
                    .build(projectTree.getNodes())
                    .buildSelected(selected)
                    .fromAndTo(node)
                    .forEach {
                        projectTree.addSelectNode(it)
                    }
            }
        }
    }

    override fun disposeAfter() {
        projectTree.dispose()
        watchService.close()
    }
}