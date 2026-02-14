package non.era.aisdme.ui.project

interface TreeState {
    fun getNodes(): List<ProjectNode>
    fun getSelectedNodes(): List<ProjectNode>
    fun reselectNode(node: ProjectNode)
    fun addSelectNode(node: ProjectNode)
}