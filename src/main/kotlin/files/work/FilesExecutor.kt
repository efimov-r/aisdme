package non.era.aisdme.files.work

import non.era.aisdme.platform.Platform
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.FileAlreadyExistsException
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
                Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES)
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
        paste(paths, isCut, targetDir)
    }

    fun paste(paths: List<Path>, isCut: Boolean, targetDir: Path) {
        findPasteIntoItself(paths, targetDir)?.let {
            throw IOException("Cannot paste directory into itself: $it") //иначе Files.walk копирует уже скопированное, вложенность бесконечная
        }

        val existing = paths.filter { Files.exists(it) } //остальные уже перемещены другим приложением
        findTakenTarget(existing, targetDir)?.let {
            throw FileAlreadyExistsException(it.toString()) //до копирования: не оставлять вставку наполовину
        }

        existing.forEach { pasteOne(it, targetDir.resolve(it.fileName), isCut) }
    }

    private fun pasteOne(src: Path, dst: Path, isCut: Boolean) {
        try {
            if (isCut) {
                Files.move(src, dst)
            } else if (Files.isDirectory(src)) {
                copyRecursive(src, dst)
            } else {
                Files.copy(src, dst)
            }
        } catch (e: AtomicMoveNotSupportedException) {// fallback для разных дисков / FS
            if (!isCut) throw e
            if (Files.isDirectory(src)) {
                copyRecursive(src, dst)
                deleteRecursive(src)
            } else {
                Files.copy(src, dst)
                Files.deleteIfExists(src)
            }
        }
    }

    fun findPasteIntoItself(targetDir: Path): Path? =
        findPasteIntoItself(getClipboardFiles().first, targetDir)

    fun findPasteIntoItself(paths: List<Path>, targetDir: Path): Path? {
        val target = targetDir.toAbsolutePath().normalize()
        return paths.firstOrNull { Files.isDirectory(it) && target.startsWith(it.toAbsolutePath().normalize()) }
    }

    /** Цель занята другим файлом. Тот же файл (имя в другом регистре на Windows) — не занята */
    fun isTargetTaken(src: Path, target: Path): Boolean =
        Files.exists(target) && !(Files.exists(src) && Files.isSameFile(src, target))

    fun findTakenTarget(paths: List<Path>, targetDir: Path): Path? =
        paths.firstNotNullOfOrNull { src ->
            targetDir.resolve(src.fileName).takeIf { isTargetTaken(src, it) }
        }

    fun findTakenTarget(targetDir: Path): Path? =
        findTakenTarget(getClipboardFiles().first, targetDir)

    private fun getClipboardFiles(): Pair<List<Path>, Boolean> {
        val cutFlavor = FileTransferable.cutFlavor //до getContents: связь с форматом ОС должна быть уже зарегистрирована
        val contents = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
            ?: return emptyList<Path>() to false

        val files = when {
            contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ->
                (contents.getTransferData(DataFlavor.javaFileListFlavor) as List<*>).map { (it as File).toPath() }

            contents.isDataFlavorSupported(FileTransferable.uriFlavor) ->
                (contents.getTransferData(FileTransferable.uriFlavor) as String).lines()
                    .filter { it.isNotBlank() && !it.startsWith("#") }
                    .map { Paths.get(URI(it.trim())) }

            else -> emptyList()
        }

        val isCut = contents.isDataFlavorSupported(cutFlavor) &&
            Platform.current.isClipboardCut((contents.getTransferData(cutFlavor) as InputStream).use { it.readBytes() })

        return files to isCut
    }

    fun move(src: Path, target: Path) {
        val caseOnly = src.toString() != target.toString() && Files.exists(target) && Files.isSameFile(src, target)
        if (caseOnly) {//Windows: Files.move не меняет только регистр, Path.equals регистр не различает
            if (!src.toFile().renameTo(target.toFile())) throw IOException("Cannot rename $src to $target")
            return
        }
        Files.move(src, target) //без REPLACE_EXISTING: занятая цель — FileAlreadyExistsException
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