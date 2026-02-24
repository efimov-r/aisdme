package non.era.aisdme.files

import non.era.aisdme.ui.project.data.ProjectNode
import java.nio.file.Path

interface IFileController {
    fun createFile(nodes: List<ProjectNode>, name: String)
    fun createDirectory(nodes: List<ProjectNode>, name: String)
    fun delete(nodes: List<ProjectNode>)
    fun copyToClipboard(nodes: List<ProjectNode>, cut: Boolean)
    fun pasteFromClipboard(target: Path)
    fun move(src: ProjectNode, dst: ProjectNode)

//    fun copy(src: Path, dst: Path)
}