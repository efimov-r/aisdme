package non.era.aisdme.configs.store

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Serializable
data class TestConfig(
    val count: Int = 0,
    val name: String = "default",
)

class ConfigStoreTest {
    @TempDir
    lateinit var dir: Path

    private val file get() = dir.resolve("conf").resolve("config.json")

    private fun store() = ConfigStore(file, TestConfig.serializer()) { TestConfig() }

    private fun writeFile(text: String) {
        file.parent.createDirectories()
        file.writeText(text)
    }

    // файла нет — создаётся вместе с папкой, значение по умолчанию
    @Test
    fun missingFileCreatedWithDefault() {
        val store = store()
        assertTrue(file.exists())
        assertEquals(TestConfig(), runBlocking { store.get() })
    }

    // битый JSON — значение по умолчанию
    @Test
    fun brokenJsonReturnsDefault() {
        writeFile("{ broken")
        assertEquals(TestConfig(), runBlocking { store().get() })
    }

    // update записывает и в кэш, и на диск
    @Test
    fun updateWritesToCacheAndDisk() {
        val store = store()
        runBlocking { store.update { it.copy(count = 5) } }

        assertEquals(5, store.getFromCashe()?.count)
        assertEquals(5, runBlocking { store().get() }.count)
    }

    // get берёт кэш, reload перечитывает файл
    @Test
    fun reloadRereadsFile() {
        val store = store()
        runBlocking { store.get() }
        writeFile("""{"count": 7}""")

        assertEquals(0, runBlocking { store.get() }.count)
        assertEquals(7, runBlocking { store.reload() }.count)
    }

    // неизвестные ключи игнорируются, недостающие — по умолчанию
    @Test
    fun unknownKeysIgnored() {
        writeFile("""{"count": 3, "removedField": true}""")
        assertEquals(TestConfig(count = 3), runBlocking { store().get() })
    }
}