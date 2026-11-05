package non.era.aisdme.ui.editor.halfviewmodel.realizations

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import non.era.aisdme.ui.editor.halfviewmodel.TabCash.OpenFileType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

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
}