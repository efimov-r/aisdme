package non.era.aisdme.ui.editor.halfviewmodel.realizations

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import non.era.aisdme.awaitUntil
import non.era.aisdme.ui.editor.halfviewmodel.TabCash.OpenFileType
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TabCashFileTextTest {
    @TempDir
    lateinit var dir: File

    private val scope = CoroutineScope(SupervisorJob())

    @AfterEach
    fun cancelScope() = scope.cancel()

    // вкладка, которая ещё не загрузила файл, не затирает его при выходе
    @Test
    fun disposeNotLoadedTabKeepsFile() {
        val file = File(dir, "a.asm").apply { writeText("data") }
        val tab = TabCashFileText(1, file, scope)

        runBlocking { tab.disposeBeforeBlocked() }

        assertEquals("data", file.readText())
    }

    // текст не меняли — файл не переписывается, не-UTF-8 байты целы
    @Test
    fun disposeUnchangedTabKeepsBytes() {
        val bytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x41)
        val file = File(dir, "a.o").apply { writeBytes(bytes) }
        val tab = TabCashFileText(1, file, scope)

        runBlocking {
            tab.openFile(OpenFileType.OPEN_NEW_FILE)
            tab.disposeBeforeBlocked()
        }

        assertContentEquals(bytes, file.readBytes())
    }

    // изменённый текст записывается при выходе
    @Test
    fun disposeEditedTabWritesText() {
        val file = File(dir, "a.asm").apply { writeText("old") }
        val tab = TabCashFileText(1, file, scope)

        runBlocking {
            tab.openFile(OpenFileType.OPEN_NEW_FILE)
            tab.updateTextFieldValue(TextFieldValue("new"))
            tab.disposeBeforeBlocked()
        }

        assertEquals("new", file.readText())
    }

    private fun openTab(text: String = "old"): Pair<File, TabCashFileText> {
        val file = File(dir, "a.asm").apply { writeText(text) }
        val tab = TabCashFileText(1, file, scope)
        runBlocking { tab.openFile(OpenFileType.OPEN_NEW_FILE) }
        return file to tab
    }

    private fun changeOnDisk(file: File, text: String) { //время правки сдвигаем: отпечаток точно другой
        file.writeText(text)
        file.setLastModified(file.lastModified() + STAMP_SHIFT_MS)
    }

    private fun check(tab: TabCashFileText) = runBlocking { tab.checkDisk() }

    private fun edit(tab: TabCashFileText, text: String) = tab.updateTextFieldValue(TextFieldValue(text))

    // файл изменили снаружи, правок во вкладке нет: текст перечитывается
    @Test
    fun cleanTabReloadsTextChangedOnDisk() {
        val (file, tab) = openTab()

        changeOnDisk(file, "external")

        assertFalse(check(tab))
        assertEquals("external", tab.textFieldValue.text)
        assertEquals(WriteStatus.WRITABLE, tab.writeStatus)
    }

    // выделение после перечитывания не выходит за новый текст
    @Test
    fun reloadClampsSelection() {
        val (file, tab) = openTab("long old text")
        tab.updateTextFieldValue(TextFieldValue("long old text", TextRange(13)))
        runBlocking { tab.beforeRemove() } //сохранить: вкладка снова без правок

        changeOnDisk(file, "ab")
        check(tab)

        assertEquals(TextRange(2), tab.textFieldValue.selection)
    }

    // есть несохранённый ввод и файл изменили: конфликт, текст на диске не трогаем
    @Test
    fun editedTabGetsConflictWhenDiskChanged() {
        val (file, tab) = openTab()
        edit(tab, "mine")

        changeOnDisk(file, "external")

        assertFalse(check(tab))
        assertEquals(WriteStatus.CONFLICT, tab.writeStatus)
        assertEquals("mine", tab.textFieldValue.text)
        assertEquals("external", file.readText())
    }

    // файл только «тронули», текст тот же: конфликта нет
    @Test
    fun touchedFileWithSameTextIsNotConflict() {
        val (file, tab) = openTab()
        edit(tab, "mine")

        changeOnDisk(file, "old")
        check(tab)

        assertEquals(WriteStatus.NOT_SAVE, tab.writeStatus)
    }

    // своя запись не считается изменением снаружи
    @Test
    fun ownSaveIsNotExternalChange() {
        val (file, tab) = openTab()
        edit(tab, "mine")
        runBlocking { tab.beforeRemove() }

        assertFalse(check(tab))

        assertEquals("mine", file.readText())
        assertEquals("mine", tab.textFieldValue.text)
        assertEquals(WriteStatus.WRITABLE, tab.writeStatus)
    }

    // сохранение не затирает правку снаружи, сделанную после последней сверки
    @Test
    fun saveDoesNotOverwriteExternalChange() {
        val (file, tab) = openTab()
        edit(tab, "mine")
        changeOnDisk(file, "external")

        runBlocking { tab.beforeRemove() }

        assertEquals("external", file.readText())
        assertEquals(WriteStatus.CONFLICT, tab.writeStatus)
    }

    // выход из приложения не затирает файл, изменённый снаружи
    @Test
    fun disposeKeepsExternalChange() {
        val (file, tab) = openTab()
        edit(tab, "mine")
        changeOnDisk(file, "external")

        runBlocking { tab.disposeBeforeBlocked() }

        assertEquals("external", file.readText())
    }

    // в конфликте клик и выделение не сбивают статус
    @Test
    fun selectionInConflictKeepsStatus() {
        val (file, tab) = openTab()
        edit(tab, "mine")
        changeOnDisk(file, "external")
        check(tab)

        tab.updateTextFieldValue(TextFieldValue("mine", TextRange(1)))

        assertEquals(WriteStatus.CONFLICT, tab.writeStatus)
    }

    // «Load from disk»: текст с диска, ввод пропадает
    @Test
    fun loadFromDiskResolvesConflict() {
        val (file, tab) = openTab()
        edit(tab, "mine")
        changeOnDisk(file, "external")
        check(tab)

        tab.resolve(ExternalChoice.LOAD_FROM_DISK)

        awaitUntil(what = "reload") { tab.writeStatus == WriteStatus.WRITABLE }
        assertEquals("external", tab.textFieldValue.text)
    }

    // «Keep mine»: следующее сохранение перезаписывает файл
    @Test
    fun keepMineOverwritesDiskOnSave() {
        val (file, tab) = openTab()
        edit(tab, "mine")
        changeOnDisk(file, "external")
        check(tab)

        tab.resolve(ExternalChoice.KEEP_MINE)
        awaitUntil(what = "status") { tab.writeStatus == WriteStatus.NOT_SAVE }
        runBlocking { tab.beforeRemove() }

        assertEquals("mine", file.readText())
    }

    // файл без правок удалили: вкладку надо закрыть
    @Test
    fun deletedCleanTabAsksToClose() {
        val (file, tab) = openTab()

        file.delete()

        assertTrue(check(tab))
    }

    // файл с несохранённым вводом удалили: вкладка остаётся с текстом
    @Test
    fun deletedEditedTabKeepsText() {
        val (file, tab) = openTab()
        edit(tab, "mine")

        file.delete()

        assertFalse(check(tab))
        assertEquals(WriteStatus.DELETED, tab.writeStatus)
        assertEquals("mine", tab.textFieldValue.text)
    }

    // «Save again»: удалённый файл создаётся заново из текста вкладки
    @Test
    fun saveAgainRecreatesDeletedFile() {
        val (file, tab) = openTab()
        edit(tab, "mine")
        file.delete()
        check(tab)

        tab.resolve(ExternalChoice.SAVE_AGAIN)

        awaitUntil(what = "recreate") { tab.writeStatus == WriteStatus.WRITABLE }
        assertEquals("mine", file.readText())
    }

    private companion object {
        const val STAMP_SHIFT_MS = 2000L
    }
}