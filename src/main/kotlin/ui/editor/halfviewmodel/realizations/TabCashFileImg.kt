package non.era.aisdme.ui.editor.halfviewmodel.realizations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import non.era.aisdme.BuildConfig
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.files.TypeFile
import non.era.aisdme.ui.editor.halfviewmodel.TabCash
import non.era.aisdme.ui.editor.halfviewmodel.TabCashFile
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import non.era.aisdme.ui.editor.halfviewmodel.help.ImageViewerState
import non.era.aisdme.utils.Log
import java.io.File
import java.io.FileInputStream

class TabCashFileImg(
    id: Int,
    file: File,
    extScope: CoroutineScope,
) : TabCashFile(id, file, extScope) {
    override val typeFile: TypeFile = TypeFile.IMG_FILE

    private var _imgBitmap by mutableStateOf<ImageBitmap?>(null)
    val imgBitmap get() = _imgBitmap

    val imageViewerState = ImageViewerState() //масштаб и сдвиг своей вкладки: сохраняются при переключении вкладок

    override suspend fun beforeRemove() {
    }

    override suspend fun disposeBeforeBlocked() {
    }

    override suspend fun openExistFile(typeOpen: OpenFileType) = withContext(Dispatchers.IO) {
        val stamp = readStamp() //до чтения: правка во время чтения будет замечена при сверке
        FileInputStream(_file).use {
            val tmp = loadImageBitmap(it)
            withContext(Dispatchers.Swing) {
                _imgBitmap = tmp
            }
        }
        diskStamp = stamp
        writeStatus(WriteStatus.READ_ONLY_FS)
    }

    override suspend fun checkDisk(): Boolean {
        if (_writeStatus != WriteStatus.READ_ONLY_FS) return false //не загружена
        val stamp = readStamp() ?: return true //файл удалён, терять нечего
        if (stamp == diskStamp) return false

        runCatching {
            val tmp = withContext(Dispatchers.IO) { FileInputStream(_file).use { loadImageBitmap(it) } }
            withContext(Dispatchers.Swing) {
                _imgBitmap = tmp
                diskStamp = stamp
            }
        }.onFailure { Log.e(it) } //файл ещё пишут или он не картинка: отпечаток не обновлён, проверим в следующий раз
        return false
    }
}