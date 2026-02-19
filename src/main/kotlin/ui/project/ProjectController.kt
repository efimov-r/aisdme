package non.era.aisdme.ui.project

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.FileOpened
import non.era.aisdme.bus.RebuilDirRiff
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File

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
    private val bus: AppEventBus
) : UiController() {

    private val treeState = ProjectTreeState()

    init {
        scope.launch {
            bus.events.collect { event ->
                when (event) {
                    is RebuilDirRiff -> {
                        scope.launch {
                            treeState.rebuildDirDiff(event.dir)
                        }
                    }
                    else ->{}
                }
            }
        }
    }

    fun reselectRootProject(config: ConfigStore<ProjectConfig>?, root: File) = scope.launch {
        val treeStateConfig = config?.get()?.treeState
        treeState.reselectRootProject(treeStateConfig, root)
    }

    fun getSelectedNodes() = treeState.getSelectedNodes()
    fun getNodes() = treeState.getNodes()
    fun cutNoteMarkRecursive(node: ProjectNode) = treeState.cutNoteMarkRecursive(node)
    fun reloadDirFromDisk(config: ConfigStore<ProjectConfig>?) = scope.launch {
        val treeStateConfig = config?.get()?.treeState
        treeState.reloadDirFromDisk(treeStateConfig)
    }
    fun saveTreeStateToConfig(config: ConfigStore<ProjectConfig>?)  { //todo добавить приоткрытии файла в редакторе
        val st = treeState.getCashForConfig()
        runBlocking {
            config?.update { it.copy(treeState = st) }
        }
    }

    private var firstMovement: Movement? = null
    fun movementOnTree(typeMovement: TypeMovementOnTree) {
        val selected = treeState.getSelectedNodes()
        when(typeMovement) {
            TypeMovementOnTree.TOP -> {
                ProjectListLineHelper.last(treeState.getNodes(), selected)
            }
            TypeMovementOnTree.SHIFT_TOP -> {
                if(firstMovement == null) firstMovement = Movement.TOP
                ProjectListLineHelper.shiftTop(treeState.getNodes(), selected, firstMovement)
                if(treeState.getSelectedNodes().size <= 1) firstMovement = null
            }
            TypeMovementOnTree.BOTTOM -> {
                ProjectListLineHelper.next(treeState.getNodes(), selected)
            }
            TypeMovementOnTree.SHIFT_BOTTOM -> {
                if(firstMovement == null) firstMovement = Movement.BOTTOM
                ProjectListLineHelper.shiftBottom(treeState.getNodes(), selected, firstMovement)
                if(treeState.getSelectedNodes().size <= 1) firstMovement = null
            }
        }
    }


    private var nodeClicked: ProjectNode? = null
    fun clickNode(node: ProjectNode, typeClick: TypeClickNode) {
        val selected = treeState.getSelectedNodes()
        when (typeClick) {
            TypeClickNode.LMB -> {
                if(selected.size <= 1){
                    nodeClicked = node
                    treeState.reselectNode(node)
                }
            }
            TypeClickNode.DOWN -> {
                if(selected.size > 1){
                    if(nodeClicked?.equalsOf(node) == true) {
                        nodeClicked = null
                    } else {
                        treeState.reselectNode(node)
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
                    treeState.reselectNode(node)
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
                ProjectListLineHelper.fromAndTo(treeState.getNodes(), selected, node)
            }
            TypeClickNode.CHEVRON -> {
                nodeClicked = node
                if (node is DirNode) {
                    treeState.reselectNode(node)
                    node.openOrClose()
                }
            }
        }
    }
}