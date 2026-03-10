package non.era.aisdme.ui.editor.halfviewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import non.era.aisdme.utils.ClipboardHelper

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

    override suspend fun beforeRemove() {
    }

    override suspend fun disposeBeforeBlocked() {
    }
}