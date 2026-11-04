package non.era.aisdme.ui.win

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.nio.file.Path

class WindowState {
    private var _projectRoot by mutableStateOf<Path?>(null)

    @Volatile // Отдельное поле для фонового чтения
    private var _projectRootValue: Path? = null

    val projectRoot: Path? get() = _projectRoot  // для UI

    fun getProjectRootSafe(): Path? = _projectRootValue  // для фона

    fun openProject(path: Path) {
        _projectRootValue = path  // сначала обновляем volatile
        _projectRoot = path       // потом State для UI
    }
}