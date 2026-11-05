package non.era.aisdme.configs

import kotlinx.coroutines.runBlocking
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.dataclass.child.SnapshotTree
import non.era.aisdme.configs.dataclass.child.TreeStateElement
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.configs.repositories.TreeStateRepository
import non.era.aisdme.configs.store.ConfigPaths
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.files.TypeFile
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConfigControllerTest {
    @TempDir
    lateinit var dir: File

    private val editorRep = EditorStateRepository()
    private val treeRep = TreeStateRepository()

    private fun controller() = ConfigController(
        editorRep = editorRep,
        treeRep = treeRep,
        globalStore = ConfigStore(File(dir, "global.json").toPath(), AppConfig.serializer()) { AppConfig() },
    )

    private fun project(name: String) = File(dir, name).apply { mkdirs() }

    private fun configOf(root: File) = runBlocking {
        ConfigStore(ConfigPaths.projectConfigFile(root.toPath()), ProjectConfig.serializer()) { ProjectConfig() }.get()
    }

    // дерево при смене проекта сохраняется в свой конфиг и не попадает в новый
    @Test
    fun treeSnapshotStaysInItsProject() {
        val a = project("a")
        val b = project("b")
        val tree = SnapshotTree(mapOf("src" to TreeStateElement(isDeer = true, expanded = true)))
        val controller = controller()

        runBlocking { controller.reselectProject(a) }
        treeRep.updateTree(tree)
        runBlocking { controller.reselectProject(b) }
        controller.dispose()

        assertEquals(tree, configOf(a).treeState)
        assertNull(configOf(b).treeState)
    }

    // вкладки при смене проекта сохраняются в свой конфиг и не попадают в новый
    @Test
    fun editorSnapshotStaysInItsProject() {
        val a = project("a")
        val b = project("b")
        val tab = SnapshotTabCash(File(a, "main.asm").path, TypeFile.TEXT_FILE)
        val controller = controller()

        runBlocking { controller.reselectProject(a) }
        editorRep.updateFiles(mapOf(0 to tab))
        editorRep.updateLeftTabs(listOf(0))
        runBlocking { controller.reselectProject(b) }
        controller.dispose()

        assertEquals(listOf(0), configOf(a).snapshotEditor?.leftTabs)
        assertNull(configOf(b).snapshotEditor)
    }
}