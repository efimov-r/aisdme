package non.era.aisdme.files.work

import java.nio.file.Path

interface IFileController {
    fun createFile(dir: Path, name: String)
    fun createDirectory(dir: Path, name: String)
    fun delete(paths: List<Path>)
    fun copyToClipboard(paths: List<Path>, cut: Boolean)
    fun pasteFromClipboard(target: Path)
    fun move(src: List<Path>, dst: Path)
    fun rename(src: Path, newName: String)

//    fun copy(src: Path, dst: Path)
}