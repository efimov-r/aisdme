package non.era.aisdme.ui.project

import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
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

    // изменения во вложенной папке тоже применяются
    @Test
    fun diffUpdatesNestedDir() {
        File(dir, "src").mkdir()
        val root = tree()
        val src = root.children[0] as DirNode

        File(dir, "src/main.asm").writeText("")
        rebuild(root)

        assertEquals(listOf("main.asm"), names(src))
    }
}