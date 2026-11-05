package non.era.aisdme.configs.dataclass.snapshots

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.files.TypeFile
import non.era.aisdme.ui.editor.halfviewmodel.TabCash.OpenFileType
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SnapshotEditorTest {
    @TempDir
    lateinit var dir: File

    private val scope = CoroutineScope(SupervisorJob())

    @AfterEach
    fun cancelScope() = scope.cancel()

    private fun projectStore() =
        ConfigStore(File(dir, "config.json").toPath(), ProjectConfig.serializer()) { ProjectConfig() }

    // вкладка с ошибкой пропускается, остальные сохраняются
    @Test
    fun brokenTabSkippedOthersSaved() {
        val ok = TabCashFileText(0, File(dir, "a.asm").apply { writeText("a") }, scope)
        val broken = TabCashFileText(1, File(dir, "missing.asm"), scope)
        runBlocking { broken.openFile(OpenFileType.OPEN_NEW_FILE) } //файла нет — ERROR

        val res = SnapshotEditor.buildFromOldVersion(
            files = mapOf(0 to ok, 1 to broken),
            leftTabs = listOf(0, 1),
            rightTabs = null,
            leftActive = 0,
            rightActive = null,
            projectStore = projectStore(),
        )

        assertEquals(setOf(0), res?.files?.keys)
        assertEquals(listOf(0, 1), res?.leftTabs)
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

        val res = SnapshotEditor.buildFromOldVersion(null, null, listOf(5), null, 5, store)

        assertEquals(old.copy(rightTabs = listOf(5), rightTabsSelect = 5), res)
    }

    // ни старого снимка, ни вкладок — сохранять нечего
    @Test
    fun nothingToSaveReturnsNull() {
        assertNull(SnapshotEditor.buildFromOldVersion(null, null, null, null, null, projectStore()))
    }
}