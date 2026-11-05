package non.era.aisdme.utils

import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class PathHelperTest {
    private val home = Path.of(System.getProperty("user.home"))

    // путь внутри домашней папки сокращается до ~
    @Test
    fun pathInsideHomeShortened() {
        val path = home.resolve("projects").resolve("demo")
        assertEquals("~${File.separator}projects${File.separator}demo", PathHelper.pathToStrFormat(path))
    }

    // сама домашняя папка — ~
    @Test
    fun homeItselfShortened() {
        assertEquals("~", PathHelper.pathToStrFormat(home))
    }

    // папка рядом с домашней, с тем же началом имени, не сокращается
    @Test
    fun siblingOfHomeUnchanged() {
        val path = home.resolveSibling(home.fileName.toString() + "2").resolve("demo")
        assertEquals(path.toString(), PathHelper.pathToStrFormat(path))
    }

    // путь вне домашней папки не меняется
    @Test
    fun pathOutsideHomeUnchanged() {
        val path = Path.of("projects", "demo")
        assertEquals(path.toString(), PathHelper.pathToStrFormat(path))
    }
}