package non.era.aisdme.files.work

import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FilesExecutorTest {
    @TempDir
    lateinit var dir: Path

    private fun file(name: String, text: String = name): Path {
        val res = dir.resolve(name)
        res.parent.createDirectories()
        res.writeText(text)
        return res
    }

    // создаётся пустой файл
    @Test
    fun createFileCreatesEmptyFile() {
        val path = dir.resolve("a.asm")
        FilesExecutor.createFile(path)
        assertEquals("", path.readText())
    }

    // существующий файл не затирается
    @Test
    fun createFileFailsIfExists() {
        val path = file("a.asm", "data")
        assertFailsWith<FileAlreadyExistsException> { FilesExecutor.createFile(path) }
        assertEquals("data", path.readText())
    }

    // создаётся папка
    @Test
    fun createDirectoryCreatesDirectory() {
        val path = dir.resolve("src")
        FilesExecutor.createDirectory(path)
        assertTrue(Files.isDirectory(path))
    }

    // удаляется файл, несуществующий путь — без ошибки
    @Test
    fun deleteRemovesFileAndIgnoresMissing() {
        val path = file("a.asm")
        FilesExecutor.delete(path)
        assertFalse(path.exists())
        FilesExecutor.delete(dir.resolve("missing"))
    }

    // удаляется непустая папка со вложенными папками
    @Test
    fun deleteRecursiveRemovesNonEmptyDirectory() {
        file("src/a.asm")
        file("src/lib/b.asm")
        FilesExecutor.deleteRecursive(dir.resolve("src"))
        assertFalse(dir.resolve("src").exists())
    }

    // несуществующий путь — без ошибки
    @Test
    fun deleteRecursiveIgnoresMissing() {
        FilesExecutor.deleteRecursive(dir.resolve("missing"))
    }

    // файл переносится
    @Test
    fun moveMovesFile() {
        val src = file("a.asm", "data")
        val target = dir.resolve("b.asm")
        FilesExecutor.move(src, target)
        assertFalse(src.exists())
        assertEquals("data", target.readText())
    }

    // существующий файл не заменяется, оба остаются как были
    @Test
    fun moveDoesNotReplaceExisting() {
        val src = file("a.asm", "a")
        val target = file("b.asm", "b")
        assertFailsWith<FileAlreadyExistsException> { FilesExecutor.move(src, target) }
        assertEquals("a", src.readText())
        assertEquals("b", target.readText())
    }

    // переименование только регистром не теряет файл
    @Test
    fun moveCaseOnlyRenameKeepsFile() {
        val src = file("a.asm", "data")
        FilesExecutor.move(src, dir.resolve("A.asm"))
        val files = dir.listDirectoryEntries()
        assertEquals(listOf("A.asm"), files.map { it.name })
        assertEquals("data", files[0].readText())
    }

    // цель занята, только если это другой существующий файл
    @Test
    fun isTargetTakenOnlyForOtherExistingFile() {
        val src = file("a.asm")
        assertFalse(FilesExecutor.isTargetTaken(src, src))
        assertFalse(FilesExecutor.isTargetTaken(src, dir.resolve("missing")))
        assertTrue(FilesExecutor.isTargetTaken(src, file("b.asm")))
    }

    // копирование: файлы и папки появляются в цели, оригиналы остаются
    @Test
    fun pasteCopyCopiesFilesAndDirectories() {
        val a = file("from/a.asm", "a")
        file("from/src/b.asm", "b")
        val target = dir.resolve("to").createDirectories()

        val moved = FilesExecutor.paste(listOf(a, dir.resolve("from/src")), false, target)

        assertEquals("a", target.resolve("a.asm").readText())
        assertEquals("b", target.resolve("src/b.asm").readText())
        assertTrue(a.exists())
        assertEquals(emptyList(), moved)
    }

    // вырезание: оригиналы исчезают, перемещённые пары возвращаются для вкладок редактора
    @Test
    fun pasteCutMovesFiles() {
        val a = file("from/a.asm", "a")
        val target = dir.resolve("to").createDirectories()

        val moved = FilesExecutor.paste(listOf(a), true, target)

        assertEquals("a", target.resolve("a.asm").readText())
        assertFalse(a.exists())
        assertEquals(listOf(a to target.resolve("a.asm")), moved)
    }

    // конфликт имён: ничего не вставляется, существующий файл не меняется
    @Test
    fun pasteDoesNotReplaceExistingAndPastesNothing() {
        val a = file("from/a.asm", "a")
        val b = file("from/b.asm", "new")
        val target = dir.resolve("to").createDirectories()
        file("to/b.asm", "old")

        assertFailsWith<FileAlreadyExistsException> { FilesExecutor.paste(listOf(a, b), false, target) }

        assertFalse(target.resolve("a.asm").exists())
        assertEquals("old", target.resolve("b.asm").readText())
    }

    // папку нельзя вставить в её подпапку
    @Test
    fun pasteIntoItselfFails() {
        file("src/lib/a.asm")
        val src = dir.resolve("src")
        assertFailsWith<IOException> { FilesExecutor.paste(listOf(src), false, src.resolve("lib")) }
        assertEquals(listOf("lib"), src.listDirectoryEntries().map { it.name })
    }

    // исчезнувший источник пропускается
    @Test
    fun pasteSkipsMissingSources() {
        val a = file("from/a.asm")
        val target = dir.resolve("to").createDirectories()

        FilesExecutor.paste(listOf(dir.resolve("from/missing.asm"), a), false, target)

        assertEquals(listOf("a.asm"), target.listDirectoryEntries().map { it.name })
    }

    // свободная цель — null, занятая — путь в цели
    @Test
    fun findTakenTargetReturnsTakenPath() {
        val a = file("from/a.asm")
        val target = dir.resolve("to").createDirectories()
        assertNull(FilesExecutor.findTakenTarget(listOf(a), target))

        val taken = file("to/a.asm")
        assertEquals(taken, FilesExecutor.findTakenTarget(listOf(a), target))
    }

    // существующая папка не пересоздаётся
    @Test
    fun createDirectoryFailsIfExists() {
        val path = dir.resolve("src").createDirectories()
        assertFailsWith<FileAlreadyExistsException> { FilesExecutor.createDirectory(path) }
    }

    // одиночный файл удаляется и рекурсивным удалением
    @Test
    fun deleteRecursiveRemovesSingleFile() {
        val path = file("a.asm")
        FilesExecutor.deleteRecursive(path)
        assertFalse(path.exists())
    }

    // папка переносится вместе с содержимым
    @Test
    fun moveMovesDirectoryWithContent() {
        file("src/lib/a.asm", "a")
        val target = dir.resolve("moved")

        FilesExecutor.move(dir.resolve("src"), target)

        assertFalse(dir.resolve("src").exists())
        assertEquals("a", target.resolve("lib/a.asm").readText())
    }

    // вырезание папки с содержимым: все файлы на новом месте, пара «откуда — куда» вернулась
    @Test
    fun pasteCutMovesNonEmptyDirectory() {
        file("from/src/a.asm", "a")
        file("from/src/lib/b.asm", "b")
        val src = dir.resolve("from/src")
        val target = dir.resolve("to").createDirectories()

        val moved = FilesExecutor.paste(listOf(src), true, target)

        assertEquals("a", target.resolve("src/a.asm").readText())
        assertEquals("b", target.resolve("src/lib/b.asm").readText())
        assertFalse(src.exists())
        assertEquals(listOf(src to target.resolve("src")), moved)
    }

    // при вырезании исчезнувший источник не попадает в результат
    @Test
    fun pasteCutSkipsMissingSourcesInResult() {
        val a = file("from/a.asm")
        val target = dir.resolve("to").createDirectories()

        val moved = FilesExecutor.paste(listOf(dir.resolve("from/missing.asm"), a), true, target)

        assertEquals(listOf(a to target.resolve("a.asm")), moved)
    }

    // копирование сохраняет пустые папки
    @Test
    fun pasteCopyKeepsEmptyDirectories() {
        dir.resolve("from/src/empty").createDirectories()
        val target = dir.resolve("to").createDirectories()

        FilesExecutor.paste(listOf(dir.resolve("from/src")), false, target)

        assertTrue(Files.isDirectory(target.resolve("src/empty")))
    }

    // пустой список ничего не вставляет
    @Test
    fun pasteEmptyListDoesNothing() {
        val target = dir.resolve("to").createDirectories()

        assertEquals(emptyList(), FilesExecutor.paste(emptyList(), true, target))
        assertEquals(emptyList(), target.listDirectoryEntries())
    }

    // папка в себя и в свою подпапку — найдена; в соседнюю с похожим именем, файл и пустой список — нет
    @Test
    fun findPasteIntoItselfFindsOnlyOwnSubtree() {
        file("src/lib/a.asm")
        file("src2/b.asm")
        val src = dir.resolve("src")

        assertEquals(src, FilesExecutor.findPasteIntoItself(listOf(src), src))
        assertEquals(src, FilesExecutor.findPasteIntoItself(listOf(src), src.resolve("lib")))
        assertNull(FilesExecutor.findPasteIntoItself(listOf(src), dir.resolve("src2")))
        assertNull(FilesExecutor.findPasteIntoItself(listOf(src), dir))
        assertNull(FilesExecutor.findPasteIntoItself(listOf(src.resolve("lib/a.asm")), src.resolve("lib")))
        assertNull(FilesExecutor.findPasteIntoItself(emptyList(), src))
    }
}