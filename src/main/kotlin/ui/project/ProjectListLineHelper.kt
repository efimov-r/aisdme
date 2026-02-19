package non.era.aisdme.ui.project

import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.ProjectNode
import kotlin.math.max
import kotlin.math.min

object ProjectListLineHelper {

    fun buildLinearNodes(nodes: List<ProjectNode>) : MutableList<ProjectNode> {
        val result: MutableList<ProjectNode> = mutableListOf()
        fun visit(node: ProjectNode) {
            result += node
            if (node is DirNode && node.expanded) {
                node.children.forEach { visit(it) }
            }
        }
        nodes.forEach { visit(it) }
        return result
    }

    private fun getNextNodeOf(linear: List<ProjectNode>, node: ProjectNode) : ProjectNode {
        val i = linear.indexOfFirst { it.equalsOf(node) }
        return linear.getOrNull(i + 1) ?: node
    }

    fun next(nodes: List<ProjectNode>, selected: List<ProjectNode>) {
        val r = getNextNodeOf(buildLinearNodes(nodes), selected.last())
        selected.forEach { it.setSelected(false) }
        r.setSelected(true)
    }

    private fun getLastNodeOf(linear: List<ProjectNode>, node: ProjectNode) : ProjectNode {
        val i = linear.indexOfFirst { it.equalsOf(node) }
        return linear.getOrNull(i - 1) ?: node
    }

    fun last(nodes: List<ProjectNode>, selected: List<ProjectNode>) {
        val r = getLastNodeOf(buildLinearNodes(nodes), selected.first())
        selected.forEach { it.setSelected(false) }
        r.setSelected(true)
    }

    private fun listPositionSelectedNodes(linear: List<ProjectNode>, selected: List<ProjectNode>): List<Int> =
        selected.mapNotNull { sn->
            linear.forEachIndexed { index, node ->
                if(node.equalsOf(sn)) {
                    return@mapNotNull index
                }
            }
            return@mapNotNull null
        }

    fun fromAndTo(nodes: List<ProjectNode>, selected: List<ProjectNode>, to: ProjectNode) {
        val linear = buildLinearNodes(nodes)
        val indexToObj = linear.withIndex().firstOrNull { it.value.equalsOf(to) }?.index ?: return

        val l = listPositionSelectedNodes(linear, selected)

        val maxSelected = l.maxOrNull() ?: run{
            to.setSelected(true)
            return
        }
        val minSelected = l.minOrNull() ?: run{
            to.setSelected(true)
            return
        }

        val max = max(maxSelected, indexToObj)
        val min = min(minSelected, indexToObj)

        linear.slice(min..max).forEach {
            it.setSelected(true)
        }
    }

    fun shiftTop(nodes: List<ProjectNode>, selected: List<ProjectNode>, firstMovement: Movement?) {
        if(selected.isEmpty()) return

        val linear = buildLinearNodes(nodes)
        if(selected.size == 1) {
            getLastNodeOf(linear, selected[0]).setSelected(true)
        } else {
            if(firstMovement == null) return

            when(firstMovement){
                Movement.TOP -> {
                    val minSelected = listPositionSelectedNodes(linear, selected).minOrNull() ?: return
                    getLastNodeOf(linear, linear[minSelected]).setSelected(true)
                }
                Movement.BOTTOM -> {
                    val maxSelected = listPositionSelectedNodes(linear, selected).maxOrNull() ?: return
                    linear[maxSelected].setSelected(false)
                }
            }
        }
    }

    fun shiftBottom(nodes: List<ProjectNode>, selected: List<ProjectNode>, firstMovement: Movement?) {
        if(selected.isEmpty()) return

        val linear = buildLinearNodes(nodes)
        if(selected.size == 1) {
            getNextNodeOf(linear, selected[0]).setSelected(true)
        } else {
            if(firstMovement == null) return

            when(firstMovement){
                Movement.TOP -> {
                    val minSelected = listPositionSelectedNodes(linear, selected).minOrNull() ?: return
                    linear[minSelected].setSelected(false)
                }
                Movement.BOTTOM -> {
                    val maxSelected = listPositionSelectedNodes(linear, selected).maxOrNull() ?: return
                    getNextNodeOf(linear, linear[maxSelected]).setSelected(true)
                }
            }
        }
    }
}