package non.era.aisdme.ui.reload

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import non.era.aisdme.awaitUntil
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.RebuilDirRiff
import non.era.aisdme.platform.Platform
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TreeReloadWatcherTest {
    @TempDir
    lateinit var dir: File

    private val testScope = CoroutineScope(Job())
    private val bus = AppEventBus()
    private val watcher = TreeReloadWatcher(bus)
    private val events = ConcurrentLinkedQueue<File>()
    private var counter = 0

    init {
        bus.subscribe(testScope) { if (it is RebuilDirRiff) events += it.dir }
    }

    @AfterEach
    fun dispose() {
        watcher.dispose()
        testScope.cancel()
    }

    private fun newFile(parent: File) = File(parent, "f${counter++}.txt").writeText("")

    /** Регистрация идёт через очередь команд, не сразу: файлы создаются, пока событие не придёт */
    private fun touchUntilEvent(target: File) {
        var polls = 0
        awaitUntil(what = "event for $target") {
            if (polls++ % TOUCH_EVERY_POLLS == 0) newFile(target)
            events.any { it == target }
        }
    }

    /** Ждёт, пока уйдут события предыдущих изменений (пачка собирается 200 мс), и забывает их */
    private fun settle() {
        Thread.sleep(SETTLE_MS)
        events.clear()
    }

    private fun quiet() = Thread.sleep(SETTLE_MS)

    // файл, созданный в корне проекта, даёт событие обновления корня
    @Test
    fun fileCreatedInRootPublishesEvent() {
        watcher.switchProject(dir.toPath())

        touchUntilEvent(dir)
    }

    // удаление файла в корне тоже даёт событие
    @Test
    fun fileDeletedInRootPublishesEvent() {
        val file = File(dir, "a.txt").apply { writeText("") }
        watcher.switchProject(dir.toPath())
        touchUntilEvent(dir)
        settle()

        file.delete()

        awaitUntil(what = "event after delete") { events.any { it == dir } }
    }

    // много изменений подряд — одно событие на папку
    @Test
    fun manyChangesArePublishedAsOneEvent() {
        watcher.switchProject(dir.toPath())
        touchUntilEvent(dir)
        settle()

        repeat(5) { newFile(dir) }

        awaitUntil(what = "event") { events.isNotEmpty() }
        quiet()
        assertEquals(1, events.count { it == dir })
    }

    // Windows: события вложенной папки приходят без регистрации — следим за всем поддеревом
    @Test
    fun nestedDirEventsComeWithoutDirsLoadedOnWindows() {
        assumeTrue(Platform.current.watchSubtree)
        val sub = File(dir, "sub").apply { mkdir() }
        watcher.switchProject(dir.toPath())

        touchUntilEvent(sub)
    }

    // Linux: вложенная папка попадает под наблюдение, только когда её прочитали в дерево
    @Test
    fun nestedDirNeedsDirsLoadedOnLinux() {
        assumeFalse(Platform.current.watchSubtree)
        val sub = File(dir, "sub").apply { mkdir() }
        watcher.switchProject(dir.toPath())
        touchUntilEvent(dir)
        settle()

        newFile(sub)
        quiet()
        assertTrue(events.none { it == sub }, "events: $events")

        watcher.dirsLoaded(listOf(sub))
        touchUntilEvent(sub)
    }

    // папка вне проекта под наблюдение не берётся
    @Test
    fun dirOutsideProjectIsNotWatched() {
        val project = File(dir, "project").apply { mkdir() }
        val outside = File(dir, "outside").apply { mkdir() }
        watcher.switchProject(project.toPath())
        touchUntilEvent(project)
        settle()

        watcher.dirsLoaded(listOf(outside))
        repeat(TOUCH_COUNT) {
            newFile(outside)
            Thread.sleep(TOUCH_PAUSE_MS)
        }
        quiet()

        assertTrue(events.none { it == outside }, "events: $events")
    }

    // смена проекта: старый больше не отслеживается, новый — да
    @Test
    fun switchProjectStopsWatchingOldRoot() {
        val first = File(dir, "first").apply { mkdir() }
        val second = File(dir, "second").apply { mkdir() }
        watcher.switchProject(first.toPath())
        touchUntilEvent(first)

        watcher.switchProject(second.toPath())
        touchUntilEvent(second)
        settle()
        repeat(TOUCH_COUNT) {
            newFile(first)
            Thread.sleep(TOUCH_PAUSE_MS)
        }
        quiet()

        assertTrue(events.none { it == first }, "events: $events")
    }

    // папка, которой к концу пачки уже нет, не обновляется
    @Test
    fun deletedDirIsNotPublished() {
        watcher.switchProject(dir.toPath())
        touchUntilEvent(dir)
        settle()
        val sub = File(dir, "sub")

        sub.mkdir()
        newFile(sub)
        sub.deleteRecursively()
        quiet()

        assertTrue(events.none { it == sub }, "events: $events")
    }

    // после dispose событий больше нет
    @Test
    fun noEventsAfterDispose() {
        watcher.switchProject(dir.toPath())
        touchUntilEvent(dir)

        watcher.dispose()
        settle()
        repeat(TOUCH_COUNT) {
            newFile(dir)
            Thread.sleep(TOUCH_PAUSE_MS)
        }
        quiet()

        assertEquals(emptyList(), events.toList())
    }

    private companion object {
        const val TOUCH_EVERY_POLLS = 20 //опрос раз в 10 мс: файл — примерно раз в 200 мс
        const val SETTLE_MS = 600L
        const val TOUCH_COUNT = 5
        const val TOUCH_PAUSE_MS = 150L
    }
}