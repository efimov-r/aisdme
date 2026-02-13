package non.era.aisdme.ui.project

interface TreeState {
    fun getNodes(): List<ProjectNode>
    fun getSelectedNode(): ProjectNode?
    fun reselectNode(node: ProjectNode?)
}