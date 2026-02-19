package non.era.aisdme.ui.project.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.io.File

abstract class ProjectNode(
    protected val _file: File,
    val depth: Int
) {
    var isCutted by mutableStateOf(false)
    private var _isSelected by mutableStateOf(false)

    fun isSelected() = _isSelected
    fun setSelected(isSelected: Boolean) {_isSelected = isSelected}

    fun equalsOf(other: ProjectNode): Boolean {
        return this._file == other._file
    }

    fun getFileName() = _file.name
    fun getFilePath() = _file.toPath()
    fun getFileString() = _file.toString()
    fun getFile() = _file

}

class DirNode(
    file: File,
    depth: Int,
    children: List<ProjectNode>,
    expand: Boolean = true
) : ProjectNode(file, depth) {
    val children: SnapshotStateList<ProjectNode> = mutableStateListOf()
    var expanded by mutableStateOf(expand)

    init {
        this.children.addAll(children)
    }

    fun openOrClose() {
        expanded = !expanded
    }
}

class FileNode(
    file: File,
    depth: Int
) : ProjectNode(file, depth)

class LinkNode(
    file: File,
    depth: Int
) : ProjectNode(file, depth)