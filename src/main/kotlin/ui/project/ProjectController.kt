package non.era.aisdme.ui.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import non.era.aisdme.IControler
import non.era.aisdme.ui.project.TreeBuilder.buildProjectTree
import java.io.File

class ProjectController(
    projectRoot: File
) : ProjectState, IControler {
    
    val root = projectRoot

    private var nodes_ by mutableStateOf<List<ProjectNode>>(emptyList())

    fun load() {
        nodes_ = buildProjectTree(root)
    }

    override fun getNodes() = nodes_

    override fun dispose() {

    }
}