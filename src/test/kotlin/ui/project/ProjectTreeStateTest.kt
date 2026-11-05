package non.era.aisdme.ui.project

import kotlinx.coroutines.runBlocking
import non.era.aisdme.ui.project.data.DirNode
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ProjectTreeStateTest {
    @TempDir
    lateinit var dir: File

    private val state = ProjectTreeState()

    private fun root() = state.getNodes()[0] as DirNode

    // в конфиг дерева попадают только корень и раскрытые папки, файлы — нет
    @Test
    fun configKeepsOnlyRootAndExpandedDirs() {
        File(dir, "src").mkdir()
        File(dir, "lib").mkdir()
        File(dir, "a.asm").writeText("")
        runBlocking { state.reselectRootProject(null, dir) }
        (root().children.first { it.getFileName() == "src" } as DirNode).expanded = true

        val paths = state.getCashForConfig().treeState.keys

        assertEquals(setOf(dir.path, File(dir, "src").path), paths)
    }

    // папки нет в дереве (создана вместе с родителем) — обновляется ближайший предок, дерево не перестраивается
    @Test
    fun unknownDirRebuildsNearestAncestor() {
        File(dir, "lib").mkdir()
        runBlocking { state.reselectRootProject(null, dir) }
        val root = root()
        val lib = root.children[0] as DirNode
        lib.expanded = true
        File(dir, "src/inner").mkdirs()
        var notFound = false

        runBlocking { state.rebuildDirDiff(File(dir, "src/inner")) { notFound = true } }

        assertFalse(notFound)
        assertSame(root, root())
        assertEquals(listOf("lib", "src"), root.children.map { it.getFileName() })
        assertTrue(lib.expanded)
    }
}