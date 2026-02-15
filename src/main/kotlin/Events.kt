package non.era.aisdme

import java.io.File

sealed interface AppEvent

data class FileOpened(val file: File) : AppEvent