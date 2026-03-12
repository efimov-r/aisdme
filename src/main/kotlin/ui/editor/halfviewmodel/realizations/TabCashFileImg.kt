package non.era.aisdme.ui.editor.halfviewmodel.realizations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.files.TypeFile
import non.era.aisdme.ui.editor.halfviewmodel.TabCash
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import non.era.aisdme.ui.editor.halfviewmodel.help.ImageViewerState
import non.era.aisdme.utils.Log
import java.io.File
import java.io.FileInputStream

//todo открывает новую при даблклике бесконечно
class TabCashFileImg(
    id: Int,
    private var _file: File,
    extScope: CoroutineScope,
) : TabCash(id, extScope) {
    override val typeFile: TypeFile = TypeFile.IMG_FILE
    override var _name by mutableStateOf(_file.name)

    private var _writeStatus by mutableStateOf(WriteStatus.LOAD)
    val writeStatus get() = _writeStatus

    private var _path by mutableStateOf(_file.toPath())
    val path get() = _path

    private var _imgBitmap by mutableStateOf<ImageBitmap?>(null)
    val imgBitmap get() = _imgBitmap

    var _imageViewerState: ImageViewerState = ImageViewerState()
    val imageViewerState get() = _imageViewerState

    override fun copy(): TabCashFileImg {
        val res = TabCashFileImg(id, File(_file.path), _extScope)
        res._imgBitmap = _imgBitmap
        res._writeStatus = _writeStatus
        res._imageViewerState = _imageViewerState.copy()
        return res
    }

    override suspend fun beforeRemove() {
    }

    override suspend fun disposeBeforeBlocked() {
    }

    override suspend fun openFile(typeOpen: OpenFileType) = withContext(Dispatchers.IO) {
        try {
            if(!_file.exists()) {
                writeStatus(WriteStatus.ERROR)
                return@withContext
            }
            FileInputStream(_file).use {
                val tmp = loadImageBitmap(it)
                Log.d("----loaded!!")
                withContext(Dispatchers.Swing) {
                    _imgBitmap = tmp
                    Log.d("----setted!!")
                }
            }
            writeStatus(WriteStatus.READ_ONLY_FS)
            Log.d("----read_only_fs!!")
        } catch (ex: Exception) {
            Log.e(ex)
            writeStatus(WriteStatus.LOCKED)
        }
    }

    override fun toSnapshot(): SnapshotTabCash {
        return SnapshotTabCash(_file.path, typeFile)
    }

    override fun refile(file: File) {
        _file = file
        _path = _file.toPath()
        _name = _file.name
    }

    private suspend fun writeStatus(status: WriteStatus) {
        withContext(Dispatchers.Swing) {
            _writeStatus = status
        }
    }
}