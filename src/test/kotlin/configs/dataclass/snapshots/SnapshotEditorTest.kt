package non.era.aisdme.configs.dataclass.snapshots

import kotlinx.coroutines.runBlocking
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.files.TypeFile
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SnapshotEditorTest {
    @TempDir
    lateinit var dir: File

    private val rep = EditorStateRepository()

    private fun projectStore() =
        ConfigStore(File(dir, "config.json").toPath(), ProjectConfig.serializer()) { ProjectConfig() }

    // без старого снимка — новый из пришедших вкладок
    @Test
    fun newSnapshotFromTabs() {
        val files = mapOf(0 to SnapshotTabCash("a.asm", TypeFile.TEXT_FILE))
        rep.updateFiles(files)
        rep.updateLeftTabs(listOf(0))
        rep.updateLeftActiveTabs(0)

        val res = SnapshotEditor.buildFromOldVersion(rep, projectStore())

        assertEquals(SnapshotEditor(files = files, leftTabs = listOf(0), leftTabsSelect = 0), res)
    }

    // старый снимок обновляется только тем, что пришло
    @Test
    fun oldSnapshotUpdatedWithNewValues() {
        val old = SnapshotEditor(
            files = mapOf(3 to SnapshotTabCash("main.asm", TypeFile.TEXT_FILE)),
            leftTabs = listOf(3),
        )
        val store = projectStore()
        runBlocking { store.update { it.copy(snapshotEditor = old) } }
        rep.updateRightTabs(listOf(5))
        rep.updateRightActiveTabs(5)

        val res = SnapshotEditor.buildFromOldVersion(rep, store)

        assertEquals(old.copy(rightTabs = listOf(5), rightTabsSelect = 5), res)
    }

    // ни старого снимка, ни вкладок — сохранять нечего
    @Test
    fun nothingToSaveReturnsNull() {
        assertNull(SnapshotEditor.buildFromOldVersion(rep, projectStore()))
    }
}