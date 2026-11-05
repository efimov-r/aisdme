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
) { //выделение, «вырезано» и позиция на экране — в ProjectTreeState
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
    expand: Boolean = true,
    loaded: Boolean = true,
) : ProjectNode(file, depth) {
    val children: SnapshotStateList<ProjectNode> = mutableStateListOf()
    var expanded by mutableStateOf(expand)
    var loaded by mutableStateOf(loaded) //false — содержимое с диска ещё не читалось, читается при раскрытии

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