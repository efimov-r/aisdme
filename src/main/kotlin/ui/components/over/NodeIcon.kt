package non.era.aisdme.ui.components.over

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode

@Composable
fun NodeIcon(
    modifier: Modifier = Modifier,
    node: ProjectNode
) {
    val icon: String = when(node) {
        is DirNode -> "folder"
        is FileNode -> "anyType"
        else -> "unknown"
    }
    SvgIcon(icon, modifier)
}