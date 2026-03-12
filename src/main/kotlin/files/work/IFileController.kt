package non.era.aisdme.files.work

import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File
import java.nio.file.Path

interface IFileController {
    fun createFile(nodes: List<ProjectNode>, name: String)
    fun createDirectory(nodes: List<ProjectNode>, name: String)
    fun delete(nodes: List<ProjectNode>)
    fun copyToClipboard(nodes: List<ProjectNode>, cut: Boolean)
    fun pasteFromClipboard(target: Path)
    fun move(src: List<ProjectNode>, dst: File)
    fun rename(src: Path, newName: String)

//    fun copy(src: Path, dst: Path)
}