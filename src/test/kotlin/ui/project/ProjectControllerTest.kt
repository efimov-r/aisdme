package non.era.aisdme.ui.project

import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import non.era.aisdme.awaitUntil
import non.era.aisdme.bus.AppEvent
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.FileOpened
import non.era.aisdme.bus.RebuilDirRiff
import non.era.aisdme.bus.ShowFileInTree
import non.era.aisdme.configs.dataclass.child.SnapshotTree
import non.era.aisdme.configs.dataclass.child.TreeStateElement
import non.era.aisdme.configs.repositories.TreeStateRepository
import non.era.aisdme.ui.errors.GlobalMessage
import non.era.aisdme.ui.errors.TypeGlobalMessage
import non.era.aisdme.ui.project.data.DirNode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectControllerTest {
    @TempDir
    lateinit var dir: File

    private val testScope = CoroutineScope(Job())
    private val bus = AppEventBus()
    private val rep = TreeStateRepository()
    private val controller = ProjectController(bus, rep)
    private val treeState = controller.treeState

    private val events = ConcurrentLinkedQueue<AppEvent>()
    private val messages = ConcurrentLinkedQueue<GlobalMessage>()

    init {
        bus.subscribe(testScope) { events += it }
        controller.subscribeMessages { messages += it }
    }

    @AfterEach
    fun dispose() {
        controller.dispose()
        testScope.cancel()
    }

    private fun files(vararg names: String) = names.forEach {
        File(dir, it).apply { parentFile.mkdirs() }.writeText("")
    }

    private fun open(config: SnapshotTree? = null) = runBlocking {
        controller.reselectRootProject(config, dir).join()
    }

    private fun root() = treeState.getNodes()[0] as DirNode

    private fun child(name: String) = root().children.first { it.getFileName() == name }

    private fun select(vararg names: String) = names.forEach { treeState.selection.set(child(it), true) }

    private fun selected() = treeState.getSelectedNodes().map { it.getFileName() }

    private fun click(name: String, type: TypeClickNode) = controller.clickNode(child(name), type)

    // обычный клик выделяет только узел под курсором
    @Test
    fun lmbSelectsOnlyClickedNode() {
        files("a.asm", "b.asm", "c.asm")
        open()
        select("a.asm")

        click("b.asm", TypeClickNode.LMB)

        assertEquals(listOf("b.asm"), selected())
    }

    // при нескольких выделенных обычный клик выделение не меняет: так их можно перетащить
    @Test
    fun lmbKeepsMultiSelection() {
        files("a.asm", "b.asm", "c.asm")
        open()
        select("a.asm", "b.asm")

        click("c.asm", TypeClickNode.LMB)

        assertEquals(listOf("a.asm", "b.asm"), selected())
    }

    // нажатие на другой узел при нескольких выделенных оставляет только его
    @Test
    fun downOnOtherNodeReselectsWhenMultiSelected() {
        files("a.asm", "b.asm", "c.asm")
        open()
        select("a.asm", "b.asm")

        click("c.asm", TypeClickNode.DOWN)

        assertEquals(listOf("c.asm"), selected())
    }

    // нажатие на узел, по которому только что кликнули правой кнопкой, выделение сохраняет — один раз
    @Test
    fun downOnLastClickedNodeKeepsMultiSelectionOnce() {
        files("a.asm", "b.asm", "c.asm")
        open()
        select("a.asm", "b.asm")
        click("b.asm", TypeClickNode.RMB)

        click("b.asm", TypeClickNode.DOWN)
        assertEquals(listOf("a.asm", "b.asm"), selected())

        click("b.asm", TypeClickNode.DOWN)
        assertEquals(listOf("b.asm"), selected())
    }

    // нажатие при одном выделенном узле ничего не меняет
    @Test
    fun downWithSingleSelectionDoesNothing() {
        files("a.asm", "b.asm")
        open()
        select("a.asm")

        click("b.asm", TypeClickNode.DOWN)

        assertEquals(listOf("a.asm"), selected())
    }

    // правый клик при одном выделенном узле выделяет узел под курсором
    @Test
    fun rmbReselectsWhenSingleSelected() {
        files("a.asm", "b.asm")
        open()
        select("a.asm")

        click("b.asm", TypeClickNode.RMB)

        assertEquals(listOf("b.asm"), selected())
    }

    // правый клик при нескольких выделенных узлах выделение не меняет: меню откроется для всех
    @Test
    fun rmbKeepsMultiSelection() {
        files("a.asm", "b.asm", "c.asm")
        open()
        select("a.asm", "b.asm")

        click("c.asm", TypeClickNode.RMB)

        assertEquals(listOf("a.asm", "b.asm"), selected())
    }

    // Ctrl+клик добавляет узел в выделение и убирает из него
    @Test
    fun ctrlLmbTogglesNode() {
        files("a.asm", "b.asm")
        open()

        click("a.asm", TypeClickNode.CTRL_LMB)
        click("b.asm", TypeClickNode.CTRL_LMB)
        assertEquals(listOf("a.asm", "b.asm"), selected())

        click("a.asm", TypeClickNode.CTRL_LMB)
        assertEquals(listOf("b.asm"), selected())
    }

    // Shift+клик выделяет все узлы от выделенного до нажатого
    @Test
    fun shiftLmbSelectsRange() {
        files("a.asm", "b.asm", "c.asm", "d.asm")
        open()
        select("a.asm")

        click("c.asm", TypeClickNode.SHIFT_LMB)

        assertEquals(listOf("a.asm", "b.asm", "c.asm"), selected())
    }

    // двойной клик по файлу просит открыть его в редакторе
    @Test
    fun dlmbOnFilePublishesFileOpened() {
        files("a.asm")
        open()

        click("a.asm", TypeClickNode.DLMB)

        awaitUntil(what = "FileOpened") { events.any { it is FileOpened && it.node.getFile() == File(dir, "a.asm") } }
    }

    // двойной клик по папке раскрывает и сворачивает её
    @Test
    fun dlmbOnDirTogglesExpanded() {
        files("src/a.asm")
        open()
        val src = child("src") as DirNode
        assertFalse(src.expanded)

        click("src", TypeClickNode.DLMB)
        assertTrue(src.expanded)

        click("src", TypeClickNode.DLMB)
        assertFalse(src.expanded)
    }

    // стрелка у папки выделяет её и раскрывает
    @Test
    fun chevronSelectsDirAndExpandsIt() {
        files("src/a.asm", "b.asm")
        open()
        select("b.asm")

        click("src", TypeClickNode.CHEVRON)

        assertEquals(listOf("src"), selected())
        assertTrue((child("src") as DirNode).expanded)
    }

    // у файла стрелки нет: выделение не меняется
    @Test
    fun chevronOnFileDoesNothing() {
        files("a.asm", "b.asm")
        open()
        select("a.asm")

        click("b.asm", TypeClickNode.CHEVRON)

        assertEquals(listOf("a.asm"), selected())
    }

    // стрелка вниз выделяет следующий узел, на последнем — остаётся на нём
    @Test
    fun bottomMovesToNextAndStaysOnLast() {
        files("a.asm", "b.asm")
        open()
        select("a.asm")

        controller.movementOnTree(TypeMovementOnTree.BOTTOM)
        assertEquals(listOf("b.asm"), selected())

        controller.movementOnTree(TypeMovementOnTree.BOTTOM)
        assertEquals(listOf("b.asm"), selected())
    }

    // стрелка вверх выделяет предыдущий узел
    @Test
    fun topMovesToPrevious() {
        files("a.asm", "b.asm", "c.asm")
        open()
        select("c.asm")

        controller.movementOnTree(TypeMovementOnTree.TOP)

        assertEquals(listOf("b.asm"), selected())
    }

    // Shift+вниз расширяет выделение вниз, Shift+вверх возвращает его обратно
    @Test
    fun shiftBottomExtendsAndShiftTopShrinks() {
        files("a.asm", "b.asm", "c.asm", "d.asm")
        open()
        select("a.asm")

        controller.movementOnTree(TypeMovementOnTree.SHIFT_BOTTOM)
        controller.movementOnTree(TypeMovementOnTree.SHIFT_BOTTOM)
        assertEquals(listOf("a.asm", "b.asm", "c.asm"), selected())

        controller.movementOnTree(TypeMovementOnTree.SHIFT_TOP)
        assertEquals(listOf("a.asm", "b.asm"), selected())

        controller.movementOnTree(TypeMovementOnTree.SHIFT_TOP)
        assertEquals(listOf("a.asm"), selected())
    }

    // Shift+вверх расширяет выделение вверх, Shift+вниз возвращает его обратно
    @Test
    fun shiftTopExtendsAndShiftBottomShrinks() {
        files("a.asm", "b.asm", "c.asm", "d.asm")
        open()
        select("d.asm")

        controller.movementOnTree(TypeMovementOnTree.SHIFT_TOP)
        controller.movementOnTree(TypeMovementOnTree.SHIFT_TOP)
        assertEquals(listOf("b.asm", "c.asm", "d.asm"), selected())

        controller.movementOnTree(TypeMovementOnTree.SHIFT_BOTTOM)
        assertEquals(listOf("c.asm", "d.asm"), selected())
    }

    // стрелка вниз при пустом выделении выделяет верхний узел
    @Test
    fun bottomWithoutSelectionSelectsTopNode() {
        files("a.asm", "b.asm")
        open()

        controller.movementOnTree(TypeMovementOnTree.BOTTOM)

        assertEquals(listOf(root().getFileName()), selected())
    }

    // стрелка вверх при пустом выделении выделяет нижний из видимых узлов
    @Test
    fun topWithoutSelectionSelectsLastVisibleNode() {
        files("src/a.asm", "b.asm")
        open()

        controller.movementOnTree(TypeMovementOnTree.TOP)

        assertEquals(listOf("b.asm"), selected())
    }

    // Shift+стрелки при пустом выделении ничего не делают
    @Test
    fun shiftMovementWithoutSelectionDoesNothing() {
        files("a.asm", "b.asm")
        open()

        controller.movementOnTree(TypeMovementOnTree.SHIFT_TOP)
        controller.movementOnTree(TypeMovementOnTree.SHIFT_BOTTOM)

        assertEquals(emptyList(), selected())
    }

    // событие диска обновляет папку: новый файл появляется в дереве
    @Test
    fun rebuildEventShowsNewFile() {
        files("a.asm")
        open()
        files("new.asm")

        bus.publish(RebuilDirRiff(dir))

        awaitUntil(what = "new.asm in tree") { root().children.any { it.getFileName() == "new.asm" } }
    }

    // событие для папки вне проекта — глобальная ошибка, дерево остаётся
    @Test
    fun rebuildEventForUnknownDirSendsError() {
        files("a.asm")
        open()
        val other = File(dir.parentFile, "not-in-project")

        bus.publish(RebuilDirRiff(other))

        awaitUntil(what = "error message") { messages.any { it.type == TypeGlobalMessage.ERROR } }
        assertTrue(messages.first().text.contains(other.path))
        awaitUntil(what = "tree rebuilt") { treeState.getNodes().size == 1 && root().children.size == 1 }
    }

    // «показать в дереве»: папки на пути раскрыты, файл выделен
    @Test
    fun showFileEventSelectsFile() {
        files("src/inner/main.asm")
        open()
        val file = File(dir, "src/inner/main.asm")

        bus.publish(ShowFileInTree(file))

        awaitUntil(what = "file selected") {
            ProjectTreeHelper.findNode(treeState.getNodes(), file)?.let { treeState.selection.isSelected(it) } == true
        }
    }

    // дерево уходит в репозиторий конфига: после открытия — только корень
    @Test
    fun openedTreeGoesToRepository() {
        files("src/a.asm")
        open()

        awaitUntil(what = "tree in repository") { rep.tree.value?.treeState?.keys == setOf(dir.path) }
    }

    // раскрытая папка читается с диска и попадает в репозиторий
    @Test
    fun expandedDirIsLoadedAndSaved() {
        files("src/a.asm")
        open()
        val src = child("src") as DirNode
        assertFalse(src.loaded)

        src.expanded = true
        Snapshot.sendApplyNotifications() //snapshotFlow в тесте не получит изменение без рассылки

        awaitUntil(what = "src loaded") { src.loaded }
        assertEquals(listOf("a.asm"), src.children.map { it.getFileName() })
        awaitUntil(what = "src in repository") { rep.tree.value?.treeState?.containsKey(File(dir, "src").path) == true }
    }

    // раскрыть выбранную: папка и все вложенные раскрыты и прочитаны
    @Test
    fun expandSelectedExpandsAllNested() {
        files("src/inner/a.asm")
        open()
        select("src")

        runBlocking { controller.expandSelected().join() }

        val src = child("src") as DirNode
        val inner = src.children.first() as DirNode
        assertTrue(src.expanded && inner.expanded && inner.loaded)
        assertEquals(listOf("a.asm"), inner.children.map { it.getFileName() })
    }

    // обновить выбранную папку из меню: новый файл появляется в дереве
    @Test
    fun reloadSelectedDirReadsNewFiles() {
        files("a.asm")
        open()
        treeState.reselectNode(root())
        files("new.asm")

        runBlocking { controller.reloadSelectedDirFromDisk().join() }

        assertEquals(listOf("a.asm", "new.asm"), root().children.map { it.getFileName() })
    }

    // дерево строится по сохранённому конфигу: раскрытая папка раскрыта и прочитана
    @Test
    fun reselectRootProjectUsesSavedTree() {
        files("src/a.asm", "lib/b.asm")
        val config = SnapshotTree(
            mapOf(
                dir.path to TreeStateElement(true, true),
                File(dir, "src").path to TreeStateElement(true, true),
            )
        )

        open(config)

        val src = child("src") as DirNode
        assertTrue(src.expanded && src.loaded)
        assertFalse((child("lib") as DirNode).expanded)
    }
}