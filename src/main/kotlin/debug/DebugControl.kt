package non.era.aisdme.debug

import androidx.compose.ui.awt.ComposeWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import non.era.aisdme.utils.Log
import java.io.File

/**
 * Пульт для отладки: читает команды из файлов `<id>.cmd` в папке из `AISDME_DEBUG_DIR`, результат пишет в `<id>.out`.
 * Без переменной окружения ничего не делает. Запускать только в debug-сборке.
 */
object DebugControl {
    private const val ENV_DIR = "AISDME_DEBUG_DIR"
    private const val POLL_MS = 100L

    suspend fun run(window: ComposeWindow) {
        val dir = System.getenv(ENV_DIR)?.let { File(it) } ?: return
        dir.mkdirs()
        Log.d("DebugControl: ${dir.absolutePath}")

        val commands = DebugCommands(window, dir)
        while (true) {
            val files = withContext(Dispatchers.IO) { dir.listFiles { f -> f.extension == "cmd" } }
            files?.sortedBy { it.name }?.forEach { commands.handle(it) }
            delay(POLL_MS)
        }
    }
}