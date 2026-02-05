package non.era.aisdme.ui.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import non.era.aisdme.ui.project.TreeBuilder.buildProjectTree
import java.io.File

class ProjectController(
    projectRoot: File
) {
    val root = projectRoot

    var nodes by mutableStateOf<List<ProjectNode>>(emptyList())
        private set

    fun load() {
        nodes = buildProjectTree(root)
    }
}