package non.era.aisdme.ui.project

import kotlinx.coroutines.*
import non.era.aisdme.AppEventBus
import non.era.aisdme.FileOpened
import non.era.aisdme.ui.UiController
import java.io.File
import java.nio.file.FileSystems

enum class TypeClickNode{
    LMB, DOWN, DLMB, RMB, CTRL_LMB, SHIFT_LMB, CHEVRON
}

enum class TypeMovementOnTree{
    TOP, SHIFT_TOP, BOTTOM, SHIFT_BOTTOM
}

enum class Movement{
    TOP, BOTTOM
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

    private var firstMovement: Movement? = null
    fun movementOnTree(typeMovement: TypeMovementOnTree) {
        val selected = projectTree.getSelectedNodes()
        when(typeMovement) {
            TypeMovementOnTree.TOP -> {
                ProjectListLineHelper.last(projectTree.getNodes(), selected)
            }
            TypeMovementOnTree.SHIFT_TOP -> {
                if(firstMovement == null) firstMovement = Movement.TOP
                ProjectListLineHelper.shiftTop(projectTree.getNodes(), selected, firstMovement)
                if(projectTree.getSelectedNodes().size <= 1) firstMovement = null
            }
            TypeMovementOnTree.BOTTOM -> {
                ProjectListLineHelper.next(projectTree.getNodes(), selected)
            }
            TypeMovementOnTree.SHIFT_BOTTOM -> {
                if(firstMovement == null) firstMovement = Movement.BOTTOM
                ProjectListLineHelper.shiftBottom(projectTree.getNodes(), selected, firstMovement)
                if(projectTree.getSelectedNodes().size <= 1) firstMovement = null
            }
        }
    }


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
            TypeClickNode.DOWN -> {
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
                } else {
                    nodeClicked = node
                }
            }
            TypeClickNode.CTRL_LMB -> {
                nodeClicked = node
                node.setSelected(!node.isSelected())
            }
            TypeClickNode.SHIFT_LMB -> {
                nodeClicked = node
                ProjectListLineHelper.fromAndTo(projectTree.getNodes(), selected, node)
            }
            TypeClickNode.CHEVRON -> {
                nodeClicked = node
                if (node is DirNode) {
                    projectTree.reselectNode(node)
                    node.openOrClose()
                }
            }
        }
    }

    override fun disposeAfter() {
        projectTree.dispose()
        watchService.close()
    }
}