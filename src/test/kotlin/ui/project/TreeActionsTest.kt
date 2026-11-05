package non.era.aisdme.ui.project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.files.work.IFileController
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.editor.halfviewmodel.TabCashFile
import non.era.aisdme.ui.project.data.DirNode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TreeActionsTest {
    @TempDir
    lateinit var dir: File

    private class FakeFiles : IFileController { //без буфера обмена и диска
        var copied: Pair<List<Path>, Boolean>? = null
        override fun copyToClipboard(paths: List<Path>, cut: Boolean) { copied = paths to cut }
        override fun createFile(dir: Path, name: String) = Unit
        override fun createDirectory(dir: Path, name: String) = Unit
        override fun delete(paths: List<Path>) = Unit
        override fun pasteFromClipboard(target: Path) = Unit
        override fun move(src: List<Path>, dst: Path) = Unit
        override fun rename(src: Path, newName: String) = Unit
    }

    private val treeState = ProjectTreeState()
    private val editor = EditorController(AppEventBus(), EditorStateRepository(), CurrentProject())
    private val files = FakeFiles()
    private val actions = TreeActions(treeState, editor, files)

    @AfterEach
    fun dispose() = editor.dispose()

    private fun root() = treeState.getNodes()[0] as DirNode

    private fun child(name: String) = root().children.first { it.getFileName() == name }

    private fun open() = runBlocking { treeState.reselectRootProject(null, dir) }

    // выделенные файлы открываются в правой половине
    @Test
    fun openSelectedInRightHalf() {
        File(dir, "a.asm").writeText("a")
        open()
        treeState.reselectNode(child("a.asm"))

        runBlocking(Dispatchers.Swing) { actions.openSelectedInEditor(Panels.RIGHT) }

        val id = editor.rightTabs.activeTabId!!
        assertEquals(File(dir, "a.asm"), (editor.getTab(id) as TabCashFile).getFile())
    }

    // «вырезать» помечает выделенные узлы и отдаёт их пути в буфер с пометкой cut
    @Test
    fun cutMarksSelectedAndCopiesPaths() {
        File(dir, "a.asm").writeText("a")
        File(dir, "b.asm").writeText("b")
        open()
        treeState.selection.set(child("a.asm"), true)

        actions.copySelectedToClipboard(cut = true)

        assertTrue(treeState.isCut(child("a.asm")))
        assertFalse(treeState.isCut(child("b.asm")))
        assertEquals(listOf(File(dir, "a.asm").toPath()) to true, files.copied)
    }
}