package non.era.aisdme.ui.project

import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.ProjectNode

object ProjectListLineHelper {

    fun buildLinearNodes(nodes: List<ProjectNode>) : MutableList<ProjectNode> {
        val result: MutableList<ProjectNode> = mutableListOf()
        fun visit(node: ProjectNode) {
            result += node
            if (node is DirNode) {
                node.children.forEach { visit(it) }
            }
        }
        nodes.forEach { visit(it) }
        return result
    }

    fun buildLinearExpandedNodes(nodes: List<ProjectNode>) : MutableList<ProjectNode> {
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

    fun next(nodes: List<ProjectNode>, selected: List<ProjectNode>, selection: TreeSelection) {
        val r = getNextNodeOf(buildLinearExpandedNodes(nodes), selected.last())
        selection.clear()
        selection.set(r, true)
    }

    private fun getLastNodeOf(linear: List<ProjectNode>, node: ProjectNode) : ProjectNode {
        val i = linear.indexOfFirst { it.equalsOf(node) }
        return linear.getOrNull(i - 1) ?: node
    }

    fun last(nodes: List<ProjectNode>, selected: List<ProjectNode>, selection: TreeSelection) {
        val r = getLastNodeOf(buildLinearExpandedNodes(nodes), selected.first())
        selection.clear()
        selection.set(r, true)
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

    fun fromAndTo(nodes: List<ProjectNode>, selected: List<ProjectNode>, to: ProjectNode, selection: TreeSelection) {
        val linear = buildLinearExpandedNodes(nodes)
        val indexToObj = linear.indexOfFirst { it.equalsOf(to) }
        if(indexToObj == -1) return

        val l = listPositionSelectedNodes(linear, selected) + indexToObj //ничего не выделено — только to
        linear.slice(l.min()..l.max()).forEach {
            selection.set(it, true)
        }
    }

    fun shiftTop(
        nodes: List<ProjectNode>,
        selected: List<ProjectNode>,
        firstMovement: Movement?,
        selection: TreeSelection,
    ) {
        if(selected.isEmpty()) return

        val linear = buildLinearExpandedNodes(nodes)
        val positions = listPositionSelectedNodes(linear, selected)
        when {
            selected.size == 1 -> selection.set(getLastNodeOf(linear, selected[0]), true)
            firstMovement == Movement.TOP ->
                positions.minOrNull()?.let { selection.set(getLastNodeOf(linear, linear[it]), true) }
            firstMovement == Movement.BOTTOM ->
                positions.maxOrNull()?.let { selection.set(linear[it], false) }
        }
    }

    fun shiftBottom(
        nodes: List<ProjectNode>,
        selected: List<ProjectNode>,
        firstMovement: Movement?,
        selection: TreeSelection,
    ) {
        if(selected.isEmpty()) return

        val linear = buildLinearExpandedNodes(nodes)
        val positions = listPositionSelectedNodes(linear, selected)
        when {
            selected.size == 1 -> selection.set(getNextNodeOf(linear, selected[0]), true)
            firstMovement == Movement.TOP ->
                positions.minOrNull()?.let { selection.set(linear[it], false) }
            firstMovement == Movement.BOTTOM ->
                positions.maxOrNull()?.let { selection.set(getNextNodeOf(linear, linear[it]), true) }
        }
    }
}