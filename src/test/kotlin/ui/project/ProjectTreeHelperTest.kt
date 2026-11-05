package non.era.aisdme.ui.project

import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ProjectTreeHelperTest {
    @TempDir
    lateinit var dir: File

    private fun tree() = ProjectTreeHelper.buildProjectTree(null, dir)!!

    private fun rebuild(node: DirNode) = ProjectTreeHelper.applyDirDiff(node, ProjectTreeHelper.readChildren(node))

    private fun names(node: DirNode) = node.children.map { it.getFileName() }

    // новый файл появляется в дереве, удалённый исчезает
    @Test
    fun diffAddsNewAndRemovesDeleted() {
        File(dir, "a.asm").writeText("")
        File(dir, "b.asm").writeText("")
        val root = tree()

        File(dir, "b.asm").delete()
        File(dir, "c.asm").writeText("")
        rebuild(root)

        assertEquals(listOf("a.asm", "c.asm"), names(root))
    }

    // существующие узлы остаются теми же объектами: раскрытие и выделение не сбрасываются
    @Test
    fun diffKeepsExistingNodes() {
        File(dir, "src").mkdir()
        File(dir, "a.asm").writeText("")
        val root = tree()
        val src = root.children[0] as DirNode
        val file = root.children[1] as FileNode
        src.expanded = true

        File(dir, "b.asm").writeText("")
        rebuild(root)

        assertSame(src, root.children[0])
        assertSame(file, root.children[1])
        assertTrue(src.expanded)
    }

    // свёрнутая папка с диска не читается: её содержимое — при раскрытии
    @Test
    fun collapsedDirNotRead() {
        File(dir, "src").mkdir()
        File(dir, "src/main.asm").writeText("")

        val src = tree().children[0] as DirNode

        assertFalse(src.loaded)
        assertEquals(emptyList(), names(src))
    }

    // один уровень diff не трогает содержимое прочитанной вложенной папки
    @Test
    fun diffKeepsLoadedNestedChildren() {
        File(dir, "src").mkdir()
        File(dir, "src/main.asm").writeText("")
        val root = tree()
        val src = root.children[0] as DirNode
        rebuild(src)

        rebuild(root)

        assertTrue(src.loaded)
        assertEquals(listOf("main.asm"), names(src))
    }

    // «раскрыть всё» читает всё поддерево
    @Test
    fun expandAllReadsWholeSubtree() {
        File(dir, "src/inner").mkdirs()
        File(dir, "src/inner/main.asm").writeText("")
        val root = tree()

        ProjectTreeHelper.applyDirDiff(root, ProjectTreeHelper.readChildren(root, expandAll = true), deep = true)

        val inner = (root.children[0] as DirNode).children[0] as DirNode
        assertTrue(inner.loaded)
        assertEquals(listOf("main.asm"), names(inner))
    }
}