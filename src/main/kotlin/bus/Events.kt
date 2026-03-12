package non.era.aisdme.bus

import non.era.aisdme.ui.project.data.FileNode
import java.io.File
import java.nio.file.Path

sealed interface AppEvent

data class FileOpened(val node: FileNode) : AppEvent
data class RebuilDirRiff(val dir: File) : AppEvent
data class ShowFileInTree(val file: File) : AppEvent
data class MoveFile(val oldFile: Path, val newFile: Path) : AppEvent //для едитора, если файл поменял путь/название
data class DeleteFile(val file: Path) : AppEvent //для едитора, если файл удолили
data class DeleteDir(val dir: Path) : AppEvent //для едитора, если dir удолили