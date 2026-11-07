package non.era.aisdme.ui.editor.halfviewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.files.TypeFile
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileImg
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashPaste
import non.era.aisdme.ui.project.CurrentProject
import org.junit.jupiter.api.AfterEach
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class HalfViewModelFactoryTest {
    private val scope = CoroutineScope(SupervisorJob())
    private val project = CurrentProject()
    private val factory = HalfViewModelFactory()

    @AfterEach
    fun cancelScope() = scope.cancel()

    private fun fromSnapshot(id: Int, snap: SnapshotTabCash) = factory.createTabFromSnapshot(id, snap, scope, project)

    // тип вкладки зависит от расширения: картинки — просмотр, остальное — текст; id идут подряд
    @Test
    fun createFileTabChoosesTypeByExtension() {
        val text = factory.createFileTab(File("a.asm"), scope)
        val img = factory.createFileTab(File("pic.PNG"), scope)

        assertIs<TabCashFileText>(text)
        assertIs<TabCashFileImg>(img)
        assertEquals(listOf(0, 1), listOf(text.id, img.id))
    }

    // вкладка из буфера обмена получает следующий id
    @Test
    fun createPasteTabTakesNextId() {
        factory.createFileTab(File("a.asm"), scope)

        val tab = factory.createPasteTab(scope, project)

        assertEquals(1, tab.id)
    }

    // сброс нумерации начинает id с нуля
    @Test
    fun clearNexTabIdRestartsNumbering() {
        factory.createFileTab(File("a.asm"), scope)

        factory.clearNexTabId()

        assertEquals(0, factory.createFileTab(File("b.asm"), scope).id)
    }

    // вкладки из снимка: тип по записи, путь у файловых вкладок обязателен
    @Test
    fun createTabFromSnapshotByType() {
        assertIs<TabCashPaste>(fromSnapshot(0, SnapshotTabCash(null, TypeFile.PASTE_TAB)))
        assertIs<TabCashFileText>(fromSnapshot(1, SnapshotTabCash("a.asm", TypeFile.TEXT_FILE)))
        assertIs<TabCashFileImg>(fromSnapshot(2, SnapshotTabCash("pic.png", TypeFile.IMG_FILE)))
        assertNull(fromSnapshot(3, SnapshotTabCash(null, TypeFile.TEXT_FILE)))
        assertNull(fromSnapshot(4, SnapshotTabCash(null, TypeFile.IMG_FILE)))
        assertNull(fromSnapshot(5, SnapshotTabCash("a.asm", null)))
    }

    // id из снимка сдвигает нумерацию вперёд: занятый id новой вкладке не достанется, меньший — не сдвигает назад
    @Test
    fun snapshotIdMovesNumberingForward() {
        fromSnapshot(5, SnapshotTabCash("a.asm", TypeFile.TEXT_FILE))
        fromSnapshot(2, SnapshotTabCash("b.asm", TypeFile.TEXT_FILE))

        assertEquals(6, factory.createFileTab(File("c.asm"), scope).id)
    }
}