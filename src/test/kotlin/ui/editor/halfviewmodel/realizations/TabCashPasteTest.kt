package non.era.aisdme.ui.editor.halfviewmodel.realizations

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import non.era.aisdme.configs.store.ConfigPaths.PROJECT_CONFIG_DIR
import non.era.aisdme.files.TypeFile
import non.era.aisdme.ui.editor.halfviewmodel.TabCash.OpenFileType
import non.era.aisdme.ui.project.CurrentProject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TabCashPasteTest {
    @TempDir
    lateinit var dir: File

    private val scope = CoroutineScope(SupervisorJob())
    private val project = CurrentProject()

    @AfterEach
    fun cancelScope() = scope.cancel()

    private fun tab(id: Int = 3) = TabCashPaste(id, scope, project)

    private fun clipFile(id: Int = 3) = File(dir, "$PROJECT_CONFIG_DIR/pastedtab/$id.clip")

    private fun savedClip(text: String, id: Int = 3) = clipFile(id).apply {
        parentFile.mkdirs()
        writeText(text)
    }

    // название вкладки и запись в снимок: у вкладки из буфера нет пути
    @Test
    fun nameAndSnapshot() {
        val tab = tab()

        assertEquals("From Clipboard", tab.name)
        val snapshot = tab.toSnapshot()
        assertNull(snapshot.pathStr)
        assertEquals(TypeFile.PASTE_TAB, snapshot.type)
    }

    // при открытии из кэша текст читается из сохранённого файла вкладки
    @Test
    fun openFromCashReadsSavedText() {
        savedClip("mov eax, 1")
        project.open(dir.toPath())
        val tab = tab()

        runBlocking { tab.openFile(OpenFileType.OPEN_FROM_CASH) }

        assertEquals("mov eax, 1", tab.textFieldValue.text)
    }

    // сохранённого файла нет: вкладка открывается пустой, без ошибки
    @Test
    fun openFromCashWithoutFileStaysEmpty() {
        project.open(dir.toPath())
        val tab = tab()

        runBlocking { tab.openFile(OpenFileType.OPEN_FROM_CASH) }

        assertEquals("", tab.textFieldValue.text)
    }

    // проект не открыт: ничего не читается и не пишется
    @Test
    fun withoutProjectNothingIsReadOrWritten() {
        savedClip("data")
        val tab = tab()

        runBlocking {
            tab.openFile(OpenFileType.OPEN_FROM_CASH)
            tab.disposeBeforeBlocked()
            tab.beforeRemove()
        }

        assertEquals("", tab.textFieldValue.text)
        assertEquals("data", clipFile().readText())
    }

    // при выходе текст вкладки пишется в файл проекта вместе с папкой для него
    @Test
    fun disposeSavesTextAndCreatesDirectory() {
        project.open(dir.toPath())
        val tab = tab()
        assertFalse(clipFile().parentFile.exists())

        runBlocking { tab.disposeBeforeBlocked() }

        assertTrue(clipFile().isFile)
    }

    // загруженный текст при выходе записывается обратно без изменений
    @Test
    fun loadedTextSurvivesDispose() {
        savedClip("line1\nline2")
        project.open(dir.toPath())
        val tab = tab()

        runBlocking {
            tab.openFile(OpenFileType.OPEN_FROM_CASH)
            tab.disposeBeforeBlocked()
        }

        assertEquals("line1\nline2", clipFile().readText())
    }

    // закрытая вкладка удаляет свой файл, чужой не трогает
    @Test
    fun beforeRemoveDeletesOnlyOwnFile() {
        savedClip("mine", id = 3)
        savedClip("other", id = 4)
        project.open(dir.toPath())

        runBlocking { tab(id = 3).beforeRemove() }

        assertFalse(clipFile(3).exists())
        assertEquals("other", clipFile(4).readText())
    }

    // файла нет: закрытие вкладки проходит без ошибки
    @Test
    fun beforeRemoveWithoutFileDoesNothing() {
        project.open(dir.toPath())

        runBlocking { tab().beforeRemove() }

        assertFalse(clipFile().exists())
    }
}