package non.era.aisdme.ui.editor

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
import non.era.aisdme.ui.win.WindowState
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
    private val controller = EditorController(bus, rep, WindowState())

    @AfterEach
    fun dispose() = controller.dispose()

    private fun onUi(block: () -> Unit) = runBlocking(Dispatchers.Swing) { block() }

    private fun waitUntil(check: () -> Boolean) = runBlocking {
        withTimeout(5000) {
            while (!check()) delay(10)
        }
    }

    private fun openFile(name: String): Int {
        val file = File(dir, name).apply { parentFile.mkdirs(); writeText(name) }
        onUi { controller.openNodes(listOf(FileNode(file, 0))) }
        val id = controller.leftTabs.activeTabId!!
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

    // перенос вкладок из правой половины в левую сохраняет их в репозитории
    @Test
    fun rightToLeftHalfKeepsTabsInRepository() {
        val id = openFile("a.asm")
        onUi {
            controller.leftTabs.closeTab(id)
            controller.rightTabs.activateTab(id)
        }

        onUi { controller.rightToLeftHalf() }

        assertEquals(listOf(id), rep.leftTabs.value)
        assertEquals(emptyList(), rep.rightTabs.value)
        assertEquals(id, rep.leftActive.value)
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