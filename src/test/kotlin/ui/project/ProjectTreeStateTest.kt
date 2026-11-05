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
    private val loadedDirs = mutableListOf<File>()

    private fun open() = runBlocking {
        state.onDirsLoaded = { loadedDirs += it }
        state.reselectRootProject(null, dir)
    }

    private fun root() = state.getNodes()[0] as DirNode

    private fun child(node: DirNode, name: String) = node.children.first { it.getFileName() == name }

    // в конфиг дерева попадают только корень и раскрытые папки, файлы — нет
    @Test
    fun configKeepsOnlyRootAndExpandedDirs() {
        File(dir, "src").mkdir()
        File(dir, "lib").mkdir()
        File(dir, "a.asm").writeText("")
        open()
        (child(root(), "src") as DirNode).expanded = true

        val paths = state.getCashForConfig().treeState.keys

        assertEquals(setOf(dir.path, File(dir, "src").path), paths)
    }

    // папки нет в дереве (создана вместе с родителем) — обновляется ближайший предок, дерево не перестраивается
    @Test
    fun unknownDirRebuildsNearestAncestor() {
        File(dir, "lib").mkdir()
        open()
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

    // событие в папке обновляет и прочитанные вложенные папки: после OVERFLOW изменения где угодно
    @Test
    fun rebuildRefreshesLoadedNestedDirs() {
        File(dir, "src").mkdir()
        open()
        val src = root().children[0] as DirNode
        runBlocking { state.loadChildren(src) }
        File(dir, "src/main.asm").writeText("")

        runBlocking { state.rebuildDirDiff(dir) {} }

        assertEquals(listOf("main.asm"), src.children.map { it.getFileName() })
    }

    // «показать в дереве» читает свёрнутые папки на пути и выделяет файл
    @Test
    fun showNodeLoadsDirsOnPath() {
        val file = File(dir, "src/inner/main.asm").apply { parentFile.mkdirs(); writeText("") }
        open()

        runBlocking { state.showNode(file) }

        val inner = child(child(root(), "src") as DirNode, "inner") as DirNode
        assertTrue(inner.expanded)
        assertTrue(state.selection.isSelected(child(inner, "main.asm")))
    }

    // выделение — по файлу: переживает перестройку дерева с новыми узлами
    @Test
    fun selectionSurvivesTreeRebuild() {
        File(dir, "src").mkdir()
        open()
        val oldRoot = root()
        state.reselectNode(child(oldRoot, "src"))

        runBlocking { state.rebuildDirDiff(dir.parentFile) {} } //папки нет в дереве: дерево строится заново

        assertTrue(oldRoot !== root())
        assertEquals(listOf(File(dir, "src")), state.getSelectedNodes().map { it.getFile() })
    }

    // удалённый файл уходит из выделения: созданный заново с тем же именем не выделен
    @Test
    fun deletedFileLeavesSelection() {
        val file = File(dir, "a.asm").apply { writeText("") }
        open()
        state.reselectNode(child(root(), "a.asm"))

        file.delete()
        runBlocking { state.rebuildDirDiff(dir) {} }
        file.writeText("")
        runBlocking { state.rebuildDirDiff(dir) {} }

        assertEquals(emptyList(), state.getSelectedNodes())
    }

    // вырезание нескольких узлов помечает все и их содержимое, в том числе прочитанное позже
    @Test
    fun cutMarksAllNodesAndLaterLoadedChildren() {
        File(dir, "src").mkdir()
        File(dir, "src/main.asm").writeText("")
        File(dir, "b.asm").writeText("")
        File(dir, "c.asm").writeText("")
        open()
        val src = child(root(), "src") as DirNode

        state.markCut(listOf(src, child(root(), "b.asm")))
        runBlocking { state.loadChildren(src) }

        assertTrue(state.isCut(src))
        assertTrue(state.isCut(child(src, "main.asm")))
        assertTrue(state.isCut(child(root(), "b.asm")))
        assertFalse(state.isCut(child(root(), "c.asm")))
    }

    // о прочитанных папках узнаёт watcher: при открытии — корень, при чтении — сама папка
    @Test
    fun loadedDirsReported() {
        File(dir, "src").mkdir()
        open()

        runBlocking { state.loadChildren(root().children[0] as DirNode) }

        assertEquals(listOf(dir, File(dir, "src")), loadedDirs)
    }
}