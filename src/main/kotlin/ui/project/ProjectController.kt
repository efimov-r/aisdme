package non.era.aisdme.ui.project

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.FileOpened
import non.era.aisdme.bus.RebuilDirRiff
import non.era.aisdme.bus.ShowFileInTree
import non.era.aisdme.configs.dataclass.child.SnapshotTree
import non.era.aisdme.configs.repositories.TreeStateRepository
import non.era.aisdme.i18n.I18n
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
    private val bus: AppEventBus,
    private val rep: TreeStateRepository,
) : UiController() {

    val treeState = ProjectTreeState()

    init {
        bus.subscribe(scope) { event ->
            when (event) {
                is RebuilDirRiff -> {
                    scope.launch {
                        treeState.rebuildDirDiff(event.dir) {
                            scope.launch {
                                newGlobalError(I18n.s.errDirNotFound(event.dir))
                            }
                        }
                    }
                }
                is ShowFileInTree -> {
                    scope.launch(Dispatchers.Swing) {
                        treeState.showNode(event.file)
                    }
                }
                else ->{}
            }
        }
        scope.launch(Dispatchers.Swing) {
            snapshotFlow { ProjectListLineHelper.buildLinearExpandedNodes(treeState.getNodes()) } //как в ui подписка
                .debounce(200)
                .collectLatest {
                    rep.updateTree(treeState.getCashForConfig()) //на ui-потоке: читает состояние дерева
                }
        }
        scope.launch(Dispatchers.Swing) { //раскрытая, но не прочитанная папка — читается с диска
            snapshotFlow {
                ProjectListLineHelper.buildLinearExpandedNodes(treeState.getNodes())
                    .filterIsInstance<DirNode>()
                    .filter { it.expanded && !it.loaded }
            }.collect { dirs ->
                dirs.forEach { if (!it.loaded) treeState.loadChildren(it) }
            }
        }
    }

    fun expandSelected() = scope.launch {
        treeState.expandSelected()
    }

    fun reselectRootProject(treeStateConfig: SnapshotTree?, root: File) = scope.launch {
        treeState.reselectRootProject(treeStateConfig, root)
    }

    fun reloadSelectedDirFromDisk() = scope.launch {
        treeState.reloadSelectedDirFromDisk()
    }

    private var firstMovement: Movement? = null
    fun movementOnTree(typeMovement: TypeMovementOnTree) {
        val selected = treeState.getSelectedNodes()
        when(typeMovement) {
            TypeMovementOnTree.TOP -> {
                ProjectListLineHelper.last(treeState.getNodes(), selected, treeState.selection)
            }
            TypeMovementOnTree.SHIFT_TOP -> {
                if(firstMovement == null) firstMovement = Movement.TOP
                ProjectListLineHelper.shiftTop(treeState.getNodes(), selected, firstMovement, treeState.selection)
                if(treeState.getSelectedNodes().size <= 1) firstMovement = null
            }
            TypeMovementOnTree.BOTTOM -> {
                ProjectListLineHelper.next(treeState.getNodes(), selected, treeState.selection)
            }
            TypeMovementOnTree.SHIFT_BOTTOM -> {
                if(firstMovement == null) firstMovement = Movement.BOTTOM
                ProjectListLineHelper.shiftBottom(treeState.getNodes(), selected, firstMovement, treeState.selection)
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
                    bus.publish(FileOpened(node))
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
                treeState.selection.set(node, !treeState.selection.isSelected(node))
            }
            TypeClickNode.SHIFT_LMB -> {
                nodeClicked = node
                ProjectListLineHelper.fromAndTo(treeState.getNodes(), selected, node, treeState.selection)
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