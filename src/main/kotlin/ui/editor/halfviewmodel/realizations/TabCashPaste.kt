package non.era.aisdme.ui.editor.halfviewmodel.realizations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.configs.dataclass.snapshots.TypeTabSnapshot
import non.era.aisdme.configs.store.ConfigPaths.PROJECT_CONFIG_DIR
import non.era.aisdme.ui.editor.halfviewmodel.TabCash
import non.era.aisdme.ui.win.WindowState
import non.era.aisdme.utils.ClipboardHelper
import non.era.aisdme.utils.Log
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class TabCashPaste(
    id: Int,
    extScope: CoroutineScope,
    val winState: WindowState
) : TabCash(id, extScope) {

    private var _textFieldValue by mutableStateOf(TextFieldValue(""))
    val textFieldValue: TextFieldValue get() = _textFieldValue

    override var _name  by mutableStateOf("From Clipboard")

    override fun copy(): TabCash {
        val res = TabCashPaste(id, _extScope, winState)
        res._textFieldValue = textFieldValue
        return res
    }

    override suspend fun beforeRemove() { //удалить файл
        try {
            val file = getPathClipFile() ?: return
            if(!file.exists()) return
            withContext(Dispatchers.IO) {
                Files.deleteIfExists(file)
            }
        } catch (ex: Exception) {
            Log.e("Ошибка удаления файла сохраненного текста буфера обмена")
            Log.e(ex)
        }
    }

    override suspend fun disposeBeforeBlocked() { //сохранить файл
        try {
            val file = getPathClipFile() ?: return
            withContext(Dispatchers.IO) {
                Files.createDirectories(file.parent)
                file.writeText(_textFieldValue.text)
            }
        } catch (ex: Exception) {
            Log.e("Ошибка сохранения файла сохраненного текста буфера обмена")
            Log.e(ex)
        }
    }

    override suspend fun openFile(typeOpen: OpenFileType) { //загрузится из файла
        when (typeOpen) {
            OpenFileType.OPEN_NEW_FILE -> {
                val text = ClipboardHelper.getClipboardText() ?: ""
                withContext(Dispatchers.Swing) {
                    _textFieldValue = TextFieldValue(text)
                }
            }

            OpenFileType.OPEN_FROM_CASH -> {
                try {
                    val file = getPathClipFile() ?: return
                    val text = file.readText()
                    withContext(Dispatchers.Swing) {
                        _textFieldValue = TextFieldValue(text)
                    }
                } catch (ex: Exception) {
                    Log.e("Ошибка загрузки файла сохраненного текста буфера обмена")
                    Log.e(ex)
                }
            }
        }
    }

    override fun toSnapshot(): SnapshotTabCash =
        SnapshotTabCash(null, TypeTabSnapshot.PASTE_TAB)

    override fun refile(file: File) {
    }

    private fun getPathClipFile(): Path? {
        val rootProjPath = winState.getProjectRootSafe() ?: return null
        return rootProjPath
            .resolve(PROJECT_CONFIG_DIR)
            .resolve("pastedtab")
            .resolve("${id}.clip")
    }
}