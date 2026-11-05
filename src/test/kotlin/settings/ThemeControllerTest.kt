package non.era.aisdme.settings

import kotlinx.coroutines.runBlocking
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.store.ConfigStore
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeControllerTest {
    @TempDir
    lateinit var dir: File

    private val store by lazy { ConfigStore(File(dir, "global.json").toPath(), AppConfig.serializer()) { AppConfig() } }
    private val controller by lazy { ThemeController(startDark = false, globalStore = store) }

    @AfterEach
    fun dispose() = controller.dispose()

    // переключение меняет тему и пишет её в глобальный конфиг
    @Test
    fun toggleSavesTheme() {
        runBlocking { controller.toggle().join() }

        assertTrue(controller.isDarkTheme)
        assertTrue(runBlocking { store.get() }.isDarkTheme)
    }

    // два переключения подряд: в конфиге то же, что на экране
    @Test
    fun doubleToggleSavesLastValue() {
        val first = controller.toggle()
        val second = controller.toggle()
        runBlocking { first.join(); second.join() }

        assertFalse(controller.isDarkTheme)
        assertFalse(runBlocking { store.get() }.isDarkTheme)
    }
}