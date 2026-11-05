package non.era.aisdme.ui.project

import non.era.aisdme.configs.dataclass.child.SnapshotTree
import non.era.aisdme.configs.dataclass.child.TreeStateElement
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ProjectTreeHelperTest {
    @TempDir
    lateinit var dir: File

    private fun tree() = ProjectTreeHelper.buildProjectTree(null, dir)!!

    private fun treeExpanded(vararg dirs: String): DirNode { //папки dirs раскрыты, как в сохранённом конфиге
        val config = SnapshotTree(
            dirs.associate { File(dir, it).path to TreeStateElement(isDeer = true, expanded = true) }
        )
        return ProjectTreeHelper.buildProjectTree(config, dir)!!
    }

    private fun rebuild(node: DirNode) = ProjectTreeHelper.applyDirDiff(node, ProjectTreeHelper.readChildren(node))

    private fun names(node: DirNode) = node.children.map { it.getFileName() }

    private fun child(node: DirNode, name: String) = node.children.first { it.getFileName() == name }

    // папки идут перед файлами, внутри — по имени без учёта регистра
    @Test
    fun buildSortsDirsFirstThenByNameIgnoringCase() {
        File(dir, "b.asm").writeText("")
        File(dir, "A.asm").writeText("")
        File(dir, "Zdir").mkdir()
        File(dir, "adir").mkdir()

        assertEquals(listOf("adir", "Zdir", "A.asm", "b.asm"), names(tree()))
    }

    // без конфига корень раскрыт, вложенные папки свёрнуты
    @Test
    fun buildWithoutConfigExpandsOnlyRoot() {
        File(dir, "src").mkdir()

        val root = tree()

        assertTrue(root.expanded)
        assertFalse((root.children[0] as DirNode).expanded)
    }

    // раскрытие папок берётся из SnapshotTree, раскрытая папка сразу прочитана
    @Test
    fun buildTakesExpandStateFromSnapshot() {
        File(dir, "src").mkdir()
        File(dir, "src/main.asm").writeText("")
        File(dir, "lib").mkdir()
        val config = SnapshotTree(
            mapOf(
                dir.path to TreeStateElement(isDeer = true, expanded = false),
                File(dir, "src").path to TreeStateElement(isDeer = true, expanded = true),
            )
        )

        val root = ProjectTreeHelper.buildProjectTree(config, dir)!!
        val src = child(root, "src") as DirNode
        val lib = child(root, "lib") as DirNode

        assertFalse(root.expanded)
        assertTrue(src.expanded)
        assertTrue(src.loaded)
        assertEquals(listOf("main.asm"), names(src))
        assertFalse(lib.expanded)
    }

    // корня нет на диске — дерева нет
    @Test
    fun buildMissingRootReturnsNull() {
        assertNull(ProjectTreeHelper.buildProjectTree(null, File(dir, "none")))
    }

    // findNode находит и папку, и файл на любой глубине
    @Test
    fun findNodeFindsNestedFileAndDir() {
        File(dir, "src/inner").mkdirs()
        File(dir, "src/inner/main.asm").writeText("")
        val nodes = listOf(treeExpanded("src", "src/inner"))

        val file = ProjectTreeHelper.findNode(nodes, File(dir, "src/inner/main.asm"))
        val inner = ProjectTreeHelper.findNode(nodes, File(dir, "src/inner"))

        assertIs<FileNode>(file)
        assertEquals(File(dir, "src/inner/main.asm"), file.getFile())
        assertIs<DirNode>(inner)
        assertNull(ProjectTreeHelper.findNode(nodes, File(dir, "none.asm")))
    }

    // findDirNode находит только папки
    @Test
    fun findDirNodeIgnoresFiles() {
        File(dir, "src").mkdir()
        File(dir, "src/main.asm").writeText("")
        val nodes = listOf(treeExpanded("src"))

        assertEquals(File(dir, "src"), ProjectTreeHelper.findDirNode(nodes, File(dir, "src"))?.getFile())
        assertNull(ProjectTreeHelper.findDirNode(nodes, File(dir, "src/main.asm")))
    }

    // findSelectedNodes возвращает выделенные узлы в порядке дерева, в том числе вложенные
    @Test
    fun findSelectedNodesInTreeOrder() {
        File(dir, "src").mkdir()
        File(dir, "src/main.asm").writeText("")
        File(dir, "a.asm").writeText("")
        val root = treeExpanded("src")
        val src = child(root, "src") as DirNode
        val main = src.children[0]
        val a = child(root, "a.asm")
        val selection = TreeSelection()
        selection.set(a, true)
        selection.set(main, true)

        assertEquals(listOf(main, a), ProjectTreeHelper.findSelectedNodes(listOf(root), selection))
        assertEquals(emptyList(), ProjectTreeHelper.findSelectedNodes(listOf(root), TreeSelection()))
    }

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

    // файл заменили папкой с тем же именем — узел пересоздаётся папкой
    @Test
    fun diffReplacesNodeWhenTypeChanged() {
        File(dir, "x").writeText("")
        val root = tree()

        File(dir, "x").delete()
        File(dir, "x").mkdir()
        rebuild(root)

        assertIs<DirNode>(root.children[0])
    }

    // прочитанные папки — сама папка и прочитанные вложенные, непрочитанные пропускаются
    @Test
    fun loadedDirsSkipsNotLoaded() {
        File(dir, "src").mkdir()
        File(dir, "lib").mkdir()
        val root = treeExpanded("src")

        assertEquals(listOf(dir, File(dir, "src")), ProjectTreeHelper.loadedDirs(root).map { it.getFile() })
        assertEquals(emptyList(), ProjectTreeHelper.loadedDirs(child(root, "lib") as DirNode))
    }

    // selectFileInTree раскрывает папки на пути и выделяет только файл
    @Test
    fun selectFileInTreeExpandsPathAndSelectsFile() {
        File(dir, "src/inner").mkdirs()
        File(dir, "src/inner/main.asm").writeText("")
        File(dir, "a.asm").writeText("")
        val root = treeExpanded("src", "src/inner")
        val src = child(root, "src") as DirNode
        val inner = src.children[0] as DirNode
        src.expanded = false
        inner.expanded = false
        val selection = TreeSelection()
        selection.set(child(root, "a.asm"), true)

        val found = ProjectTreeHelper.selectFileInTree(listOf(root), File(dir, "src/inner/main.asm"), selection)

        assertTrue(found)
        assertTrue(src.expanded)
        assertTrue(inner.expanded)
        assertEquals(listOf(inner.children[0]), ProjectTreeHelper.findSelectedNodes(listOf(root), selection))
    }

    // файла нет в дереве — выделение не меняется
    @Test
    fun selectFileInTreeMissingKeepsSelection() {
        File(dir, "a.asm").writeText("")
        val root = tree()
        val a = child(root, "a.asm")
        val selection = TreeSelection()
        selection.set(a, true)

        val found = ProjectTreeHelper.selectFileInTree(listOf(root), File(dir, "none.asm"), selection)

        assertFalse(found)
        assertEquals(listOf(a), ProjectTreeHelper.findSelectedNodes(listOf(root), selection))
    }
}