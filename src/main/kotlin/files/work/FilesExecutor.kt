package non.era.aisdme.files.work

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes

object FilesExecutor {
    fun createFile(path: Path) {
        Files.createFile(path)
    }

    fun createDirectory(path: Path) {
        Files.createDirectory(path)
    }

    fun delete(path: Path) {
        Files.deleteIfExists(path)
    }

    fun deleteRecursive(path: Path) {
        if (!Files.exists(path)) return

        Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.deleteIfExists(file)
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                Files.deleteIfExists(dir)
                return FileVisitResult.CONTINUE
            }
        })
    }

    private fun copyRecursive(src: Path, dst: Path) {
        Files.walk(src).forEach { source ->
            val target = dst.resolve(src.relativize(source))
            if (Files.isDirectory(source)) {
                Files.createDirectories(target)
            } else {
                Files.copy(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
                )
            }
        }
    }

    fun copyToClipboard(paths: List<Path>, cut: Boolean) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard

        val transferable = FileTransferable(
            paths.map { it.toFile() },
            cut
        )

        clipboard.setContents(transferable, null)
    }

    fun pasteFromClipboard(targetDir: Path) {
        val (paths, isCut) = getClipboardFiles()

        for (src in paths) {

            val dst = targetDir.resolve(src.fileName)

            if (!Files.exists(src)) {// файл уже перемещён другим приложением
                continue
            }

            try {
                if (isCut) {// ПРАВИЛЬНЫЙ способ для CUT
                    Files.move(
                        src,
                        dst,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                } else {
                    // COPY
                    if (Files.isDirectory(src)) {
                        copyRecursive(src, dst)
                    } else {
                        Files.copy(
                            src,
                            dst,
                            StandardCopyOption.REPLACE_EXISTING
                        )
                    }
                }
            } catch (e: AtomicMoveNotSupportedException) {// fallback для разных дисков / FS
                if (isCut) {
                    if (Files.isDirectory(src)) {
                        copyRecursive(src, dst)
                        deleteRecursive(src)
                    } else {
                        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING)
                        Files.deleteIfExists(src)
                    }
                }
            }
        }
    }

    private fun getClipboardFiles(): Pair<List<Path>, Boolean> {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val contents = clipboard.getContents(null)

        val files = mutableListOf<Path>()
        var isCut = false

        // Windows Explorer
        if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            val list = contents.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
            files.addAll(list.map { it.toPath() })
        }

        // Linux / macOS (Dolphin, Nautilus)
        val uriFlavor = DataFlavor("text/uri-list;class=java.lang.String")

        if (contents.isDataFlavorSupported(uriFlavor)) {
            val data = contents.getTransferData(uriFlavor) as String

            data.lines()
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .map { URI(it.trim()) }
                .forEach { files.add(Paths.get(it)) }
        }

        // CUT flag
        val cutFlavor = DataFlavor("application/x-kde-cutselection;class=java.lang.String")

        if (contents.isDataFlavorSupported(cutFlavor)) {
            isCut = true
        }

        return files to isCut
    }

    fun move(src: Path, target: Path) {
        Files.move(src, target, StandardCopyOption.REPLACE_EXISTING)
    }

//    fun copy(src: Path, dst: Path) {
//        Files.copy(
//            src,
//            dst,
//            StandardCopyOption.REPLACE_EXISTING,
//            StandardCopyOption.COPY_ATTRIBUTES
//        )
//    }
}