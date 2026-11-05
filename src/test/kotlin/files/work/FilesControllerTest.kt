package non.era.aisdme.files.work

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import non.era.aisdme.bus.AppEvent
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.DeleteDir
import non.era.aisdme.bus.DeleteFile
import non.era.aisdme.ui.errors.GlobalMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilesControllerTest {
    @TempDir
    lateinit var dir: File

    private val bus = AppEventBus()
    private val controller = FilesController(bus)
    private val errors = CopyOnWriteArrayList<GlobalMessage>() //пишется на ui-потоке
    private val events = CopyOnWriteArrayList<AppEvent>()
    private val eventsScope = CoroutineScope(Dispatchers.IO)

    init {
        controller.subscribeMessages { errors += it }
        bus.subscribe(eventsScope) { events += it }
    }

    @AfterEach
    fun dispose() {
        controller.dispose()
        eventsScope.cancel()
    }

    private fun waitUntil(check: () -> Boolean) = runBlocking {
        withTimeout(5000) {
            while (!check()) delay(10)
        }
    }

    private fun lastEvent(): AppEvent? = events.lastOrNull()

    // удаление папки удаляет её с содержимым и публикует DeleteDir
    @Test
    fun deleteDirPublishesDeleteDir() {
        val sub = File(dir, "src").apply { mkdirs() }
        File(sub, "a.asm").writeText("a")

        controller.delete(listOf(sub.toPath()))

        waitUntil { lastEvent() != null }
        assertEquals(DeleteDir(sub.toPath()), lastEvent())
        assertFalse(sub.exists())
    }

    // удаление файла публикует DeleteFile
    @Test
    fun deleteFilePublishesDeleteFile() {
        val file = File(dir, "a.asm").apply { writeText("a") }

        controller.delete(listOf(file.toPath()))

        waitUntil { lastEvent() != null }
        assertEquals(DeleteFile(file.toPath()), lastEvent())
        assertFalse(file.exists())
    }

    // копирование файлов из разных папок: ошибка
    @Test
    fun copyFromDifferentDirsShowsError() {
        val a = File(dir, "a/1.asm").toPath()
        val b = File(dir, "b/2.asm").toPath()

        controller.copyToClipboard(listOf(a, b), cut = false)

        waitUntil { errors.isNotEmpty() }
    }

    // файл создаётся в переданной папке
    @Test
    fun createFileInDir() {
        controller.createFile(dir.toPath(), "new.asm")

        waitUntil { File(dir, "new.asm").exists() }
        assertTrue(File(dir, "new.asm").isFile)
    }
}