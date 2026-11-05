package non.era.aisdme.ui.components.dialogs.elements

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import non.era.aisdme.ui.errors.GlobalMessage
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import org.junit.jupiter.api.AfterEach
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DialogsControllerTest {
    private var selected: List<ProjectNode> = listOf()
    private val controller = DialogsController { selected }
    private val errors = CopyOnWriteArrayList<GlobalMessage>() //пишется на ui-потоке

    init {
        controller.subscriptionError { errors += it }
        runBlocking(Dispatchers.Swing) {} //подписка запускается на ui-потоке: дождаться её
    }

    @AfterEach
    fun dispose() = controller.dispose()

    private val dir = DirNode(File("dir"), 0, listOf())
    private val file = FileNode(File("a.asm"), 0)

    private fun nextDialog() = runBlocking { withTimeoutOrNull(300) { controller.effects.first() } }

    private fun waitError() = runBlocking {
        withTimeout(5000) {
            while (errors.isEmpty()) delay(10)
        }
    }

    // Paste без выделения, в несколько узлов и в файл: ошибка, диалога нет
    @Test
    fun pasteWithoutSingleDirShowsError() {
        listOf(listOf(), listOf(dir, file), listOf(file)).forEach { nodes ->
            errors.clear()
            selected = nodes

            controller.requestPasteDialog()

            waitError()
            assertNull(nextDialog())
        }
    }

    // Paste в одну папку: диалог с этой папкой
    @Test
    fun pasteIntoSingleDirOpensDialog() {
        selected = listOf(dir)

        controller.requestPasteDialog()

        assertEquals(UiDialog.ShowPasteDialog(dir), nextDialog())
    }

    // Rename нескольких узлов: ошибка, диалога нет
    @Test
    fun renameMultipleShowsError() {
        selected = listOf(dir, file)

        controller.requestRenameDialog()

        waitError()
        assertNull(nextDialog())
    }

    // Delete без выделения: диалога нет, ошибки тоже
    @Test
    fun deleteWithoutSelectionDoesNothing() {
        controller.requestDeleteDialog()

        assertNull(nextDialog())
        assertEquals(emptyList(), errors)
    }

    // Delete: диалог с узлами, выделенными в момент запроса
    @Test
    fun deleteOpensDialogWithSelectedAtRequest() {
        selected = listOf(file)

        controller.requestDeleteDialog()
        selected = listOf()

        assertEquals(UiDialog.ShowDeleteDialog(listOf(file)), nextDialog())
    }
}