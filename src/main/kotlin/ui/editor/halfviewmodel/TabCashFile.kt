package non.era.aisdme.ui.editor.halfviewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import non.era.aisdme.BuildConfig
import non.era.aisdme.bus.ShowFileInTree
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.utils.Log
import java.io.File

abstract class TabCashFile(
    id: Int,
    protected var _file: File,
    extScope: CoroutineScope,
) : TabCash(id, extScope) {
    override var _name by mutableStateOf(_file.name)

    protected var _writeStatus by mutableStateOf(WriteStatus.LOAD)
    val writeStatus get() = _writeStatus

    private var _path by mutableStateOf(_file.toPath())
    val path get() = _path

    override fun toSnapshot(): SnapshotTabCash? {
        if(!writeStatus.toConfig) return null
        return SnapshotTabCash(_file.path, typeFile)
    }

    protected abstract suspend fun openExistFile(typeOpen: OpenFileType)

    override suspend fun openFile(typeOpen: OpenFileType) {
        if(BuildConfig.DEBUG) delay(300)

        try {
            if(!_file.exists()) {
                writeStatus(WriteStatus.ERROR)
                return
            }
            openExistFile(typeOpen)
        } catch (ex: Throwable) {
            Log.e(ex)
            writeStatus(WriteStatus.LOCKED)
        }
    }

    override fun refile(file: File) {
        _file = file
        _path = _file.toPath()
        _name = _file.name
    }

    protected suspend fun writeStatus(status: WriteStatus) {
        withContext(Dispatchers.Swing) {
            _writeStatus = status
        }
    }

    fun getFile() = _file
    fun compareFile(file: File) = (_file == file)
    fun showFileInTreeEvent() = ShowFileInTree(_file)
}