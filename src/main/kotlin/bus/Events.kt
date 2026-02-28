package non.era.aisdme.bus

import java.io.File

sealed interface AppEvent

data class FileOpened(val file: File) : AppEvent
data class RebuilDirRiff(val dir: File) : AppEvent
data class ShowFileInTree(val file: File) : AppEvent