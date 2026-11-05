package non.era.aisdme.ui.editor

import androidx.compose.runtime.derivedStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withTimeout
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.DeleteDir
import non.era.aisdme.bus.MoveFile
import non.era.aisdme.configs.dataclass.snapshots.SnapshotEditor
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.files.TypeFile
import non.era.aisdme.ui.editor.halfviewmodel.TabCashFile
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.CurrentProject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditorControllerTest {
    @TempDir
    lateinit var dir: File

    private val rep = EditorStateRepository()
    private val bus = AppEventBus()
    private val controller = EditorController(bus, rep, CurrentProject())

    @AfterEach
    fun dispose() = controller.dispose()

    private fun onUi(block: () -> Unit) = runBlocking(Dispatchers.Swing) { block() }

    private fun waitUntil(check: () -> Boolean) = runBlocking {
        withTimeout(5000) {
            while (!check()) delay(10)
        }
    }

    private fun openFile(name: String, panel: Panels = Panels.LEFT): Int {
        val file = File(dir, name).apply { parentFile.mkdirs(); writeText(name) }
        onUi { controller.openNodes(listOf(FileNode(file, 0)), panel) }
        onUi {} //объединение половин — отложенно, в очереди ui-потока: дождаться
        val half = if(panel == Panels.LEFT) controller.leftTabs else controller.rightTabs
        val id = half.activeTabId!!
        waitUntil { rep.files.value?.containsKey(id) == true } //открытие дошло до конфига
        return id
    }

    // закрытая вкладка удаляется из кэша и из конфига
    @Test
    fun closedTabRemovedFromCacheAndConfig() {
        val id = openFile("a.asm")

        onUi { controller.leftTabs.closeTab(id) }
        runBlocking { controller.removeClosedTabs().join() }

        assertNull(controller.getTab(id))
        assertFalse(rep.files.value.orEmpty().containsKey(id))
    }

    // вкладка, перенесённая в другую половину, остаётся
    @Test
    fun movedTabKept() {
        val id = openFile("a.asm")

        onUi {
            controller.leftTabs.closeTab(id)
            controller.rightTabs.activateTab(id)
        }
        runBlocking { controller.removeClosedTabs().join() }

        assertNotNull(controller.getTab(id))
    }

    // вкладка, файл которой не открылся, в конфиг не попадает
    @Test
    fun brokenTabNotSavedToConfig() {
        val ok = openFile("a.asm")
        onUi { controller.openNodes(listOf(FileNode(File(dir, "missing.asm"), 0))) }
        val broken = controller.leftTabs.activeTabId!!

        waitUntil { (controller.getTab(broken) as TabCashFile).writeStatus == WriteStatus.ERROR }
        waitUntil { rep.files.value?.containsKey(ok) == true }
        runBlocking { delay(100) } //сохранение после открытия
        assertFalse(rep.files.value.orEmpty().containsKey(broken))
    }

    // левая половина опустела, а в правой есть вкладки: правая переносится в левую, репозиторий тоже
    @Test
    fun emptyLeftHalfTakesRightTabs() {
        val id = openFile("a.asm")
        onUi { controller.rightTabs.activateTab(id) } //та же вкладка в обеих половинах

        onUi { controller.leftTabs.closeTab(id) }

        waitUntil { controller.leftTabs.listTabs.toList() == listOf(id) }
        assertTrue(controller.rightTabs.listTabs.isEmpty())
        assertEquals(listOf(id), rep.leftTabs.value)
        assertEquals(emptyList(), rep.rightTabs.value)
        assertEquals(id, rep.leftActive.value)
    }

    // файл, открытый справа при пустой левой половине, оказывается слева
    @Test
    fun openInRightWithEmptyLeftGoesLeft() {
        val file = File(dir, "a.asm").apply { writeText("a") }

        onUi { controller.openNodes(listOf(FileNode(file, 0)), Panels.RIGHT) }
        onUi {} //объединение половин — в очереди ui-потока

        val id = controller.leftTabs.activeTabId!!
        assertEquals(file, (controller.getTab(id) as TabCashFile).getFile())
        assertTrue(controller.rightTabs.listTabs.isEmpty())
    }

    // «Split and Move» последней вкладки слева: вкладка не теряется, хотя половины объединяются
    @Test
    fun splitAndMoveLastLeftTabKeepsTab() {
        val a = openFile("a.asm")
        val b = openFile("b.asm", Panels.RIGHT)

        onUi {
            val left = controller.leftTabs
            val right = controller.rightTabs
            left.closeTab(a) //как в EditorTabContextMenu: ссылки на половины взяты до операции
            right.activateTab(a)
        }

        waitUntil { controller.rightTabs.listTabs.isEmpty() }
        assertEquals(listOf(b, a), controller.leftTabs.listTabs.toList())
        runBlocking { controller.removeClosedTabs().join() }
        assertNotNull(controller.getTab(a))
    }

    // derivedStateOf над левой половиной видит половину, перенесённую справа
    @Test
    fun derivedStateSeesRightToLeftHalf() {
        val id = openFile("a.asm")
        onUi { controller.rightTabs.activateTab(id) }
        val leftList = derivedStateOf { controller.leftTabs }
        val before = leftList.value

        onUi { controller.leftTabs.closeTab(id) }

        waitUntil { leftList.value !== before }
        assertEquals(listOf(id), leftList.value.listTabs.toList())
    }

    // derivedStateOf над getTab видит вкладку, добавленную в кэш позже
    @Test
    fun derivedStateSeesNewTab() {
        val a = File(dir, "a.asm").apply { writeText("a") }
        val tab = derivedStateOf { controller.getTab(0) }
        assertNull(tab.value)

        onUi {
            controller.reselectProject(
                SnapshotEditor(files = mapOf(0 to SnapshotTabCash(a.path, TypeFile.TEXT_FILE)), leftTabs = listOf(0))
            )
        }

        assertNotNull(tab.value)
    }

    // нумерация вкладок у каждого контроллера своя: первая вкладка второго — тоже 0
    @Test
    fun tabIdsIndependentPerController() {
        val other = EditorController(AppEventBus(), EditorStateRepository(), CurrentProject())
        try {
            val file = File(dir, "b.asm").apply { writeText("b") }
            onUi {
                controller.openNodes(listOf(FileNode(file, 0)))
                other.openNodes(listOf(FileNode(file, 0)))
            }

            assertEquals(0, controller.leftTabs.activeTabId)
            assertEquals(0, other.leftTabs.activeTabId)
        } finally {
            other.dispose()
        }
    }

    // после открытия проекта новая вкладка получает id больше, чем у вкладок из конфига
    @Test
    fun newTabIdAfterSnapshotIds() {
        val a = File(dir, "a.asm").apply { writeText("a") }
        onUi {
            controller.reselectProject(
                SnapshotEditor(files = mapOf(5 to SnapshotTabCash(a.path, TypeFile.TEXT_FILE)), leftTabs = listOf(5))
            )
        }

        val id = openFile("b.asm")

        assertEquals(6, id)
    }

    // вкладка файла внутри переименованной папки получает новый путь
    @Test
    fun movedDirRefilesTabsInside() {
        val id = openFile("src/a.asm")
        val tab = controller.getTab(id) as TabCashFile

        bus.publish(MoveFile(File(dir, "src").toPath(), File(dir, "lib").toPath()))

        waitUntil { tab.getFile() == File(dir, "lib/a.asm") }
    }

    // удаление папки foo не закрывает вкладку из папки foobar
    @Test
    fun deletedDirClosesOnlyItsTabs() {
        val inside = openFile("foo/a.asm")
        val sibling = openFile("foobar/b.asm")

        bus.publish(DeleteDir(File(dir, "foo").toPath()))

        waitUntil { !controller.leftTabs.listTabs.contains(inside) }
        assertTrue(controller.leftTabs.listTabs.contains(sibling))
    }

    // при открытии проекта закрытые вкладки из конфига не загружаются и уходят из конфига
    @Test
    fun reselectProjectSkipsClosedTabs() {
        val a = File(dir, "a.asm").apply { writeText("a") }
        val b = File(dir, "b.asm").apply { writeText("b") }
        val snapshot = SnapshotEditor(
            files = mapOf(
                0 to SnapshotTabCash(a.path, TypeFile.TEXT_FILE),
                1 to SnapshotTabCash(b.path, TypeFile.TEXT_FILE),
            ),
            leftTabs = listOf(0),
        )

        onUi { controller.reselectProject(snapshot) }

        assertNotNull(controller.getTab(0))
        assertNull(controller.getTab(1))
        waitUntil { rep.files.value?.keys == setOf(0) }
    }
}