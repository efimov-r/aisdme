package non.era.aisdme.ui.components.contexmenu

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable
import non.era.aisdme.LocalAppController
import non.era.aisdme.files.FileFMOpener
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.project.FileNode


@Composable
fun ProjectFileContextMenu(node: FileNode, content: @Composable () -> Unit) {
    val app = LocalAppController.current
    ContextMenuArea(
        items = {
            listOf(
                ContextMenuItem("Open in Left", onClick = { app.openFileInEditor(node.file, Panels.LEFT) }),
                ContextMenuItem("Open in Right", onClick = { app.openFileInEditor(node.file, Panels.RIGHT) }),
                ContextMenuItem("Open in Dolphin", onClick = { FileFMOpener.openFileInFileManager(node.file) }),
            )
        },
        content = content
    )
}