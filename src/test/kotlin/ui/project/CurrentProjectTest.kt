package non.era.aisdme.ui.project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class CurrentProjectTest {
    private val project = CurrentProject()

    // корень, открытый с ui-потока, виден в фоновом потоке
    @Test
    fun rootOpenedOnUiVisibleOnIo() {
        val root = Path.of("proj")

        runBlocking(Dispatchers.Swing) { project.open(root) }

        assertEquals(root, runBlocking { withContext(Dispatchers.IO) { project.root } })
    }
}