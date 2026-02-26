package non.era.aisdme.bus

import non.era.aisdme.ui.editor.SnapshotEditor
import java.io.File

sealed interface AppEvent

data class FileOpened(val file: File) : AppEvent
data class RebuilDirRiff(val dir: File) : AppEvent
data class SaveSnapshotEditor(val snapshot: SnapshotEditor) : AppEvent