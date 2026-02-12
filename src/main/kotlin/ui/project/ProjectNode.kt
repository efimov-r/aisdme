package non.era.aisdme.ui.project

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.io.File

sealed class ProjectNode {
    abstract val file: File
    abstract val depth: Int
    var isCutted by mutableStateOf(false)
}

class DirNode : ProjectNode {
    override val file: File
    override val depth: Int
    val children: SnapshotStateList<ProjectNode>
    val expanded: MutableState<Boolean>

    constructor(
        file: File,
        depth: Int,
        children: List<ProjectNode>,
        expanded: MutableState<Boolean> = mutableStateOf(true)
    ) : super() {
        this.file = file
        this.depth = depth
        this.children = mutableStateListOf()
        this.children.addAll(children)
        this.expanded = expanded
    }
}

class FileNode(
    override val file: File,
    override val depth: Int
) : ProjectNode()

class LinkNode(
    override val file: File,
    override val depth: Int
) : ProjectNode()