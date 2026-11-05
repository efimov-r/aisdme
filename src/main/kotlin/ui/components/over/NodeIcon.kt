package non.era.aisdme.ui.components.over

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import non.era.aisdme.files.TypeFile
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.nio.file.Path

@Composable
fun NodeIcon(
    modifier: Modifier = Modifier,
    node: ProjectNode?
) {
    val icon: String = when(node) {
        is DirNode -> "folder"
        is FileNode -> fileIconName(node.getFilePath())
        else -> "unknown"
    }
    SvgIcon(icon, modifier)
}

@Composable
fun FileIcon( //для вкладки: по типу файла, без поиска узла в дереве
    modifier: Modifier = Modifier,
    path: Path
) {
    SvgIcon(fileIconName(path), modifier)
}

private fun fileIconName(path: Path) = if(TypeFile.isImageFile(path)) "image" else "anyType"