package non.era.aisdme.ui.editor.halfviewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import non.era.aisdme.configs.GlobalConfig
import non.era.aisdme.utils.ClipboardHelper
import non.era.aisdme.utils.Log
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ObservableOpenedHalfPaste(
    id: Int,
    extScope: CoroutineScope
) : ObservableOpenedHalf(id, extScope) {

    private var _textFieldValue by mutableStateOf(TextFieldValue(ClipboardHelper.getClipboardText() ?: ""))
    val textFieldValue: TextFieldValue get() = _textFieldValue

    override var _name  by mutableStateOf("From Clipboard")

    override fun copy(): ObservableOpenedHalf {
        val res = ObservableOpenedHalfPaste(id, _extScope)
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
            file.writeText(_textFieldValue.text)
        } catch (ex: Exception) {
            Log.e("Ошибка сохранения файла сохраненного текста буфера обмена")
            Log.e(ex)
        }
    }

    override suspend fun openFile() { //загрузится из файла
        try {
            val file = getPathClipFile() ?: return
            _textFieldValue = TextFieldValue(file.readText())
        } catch (ex: Exception) {
            Log.e("Ошибка загрузки файла сохраненного текста буфера обмена")
            Log.e(ex)
        }
    }

    private suspend fun getPathClipFile(): Path? {
        val rootProjPathStr = GlobalConfig.store.get().lastOpenProject ?: return null
        return File(rootProjPathStr)
            .toPath()
            .resolve(".aisdme")
            .resolve("${id}.clip")
    }
}