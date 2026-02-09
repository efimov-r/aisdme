package non.era.aisdme.ui.project

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.io.File

sealed class ProjectNode {
    abstract val file: File
    abstract val depth: Int
}

class DirNode(
    override val file: File,
    override val depth: Int,
    val children: List<ProjectNode>,
    val expanded: MutableState<Boolean> = mutableStateOf(true)
) : ProjectNode()

class FileNode(
    override val file: File,
    override val depth: Int
) : ProjectNode()

class LinkNode(
    override val file: File,
    override val depth: Int
) : ProjectNode()