package non.era.aisdme.configs.store

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigPathsTest {
    // конфиг проекта — <root>/.aisdme/config.json
    @Test
    fun projectConfigFileInsideProject() {
        val root = Path.of("projects", "demo")
        assertEquals(Path.of("projects", "demo", ".aisdme", "config.json"), ConfigPaths.projectConfigFile(root))
    }
}