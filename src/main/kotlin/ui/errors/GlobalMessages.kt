package non.era.aisdme.ui.errors

import androidx.compose.runtime.mutableStateListOf
import non.era.aisdme.ui.UiController

class GlobalMessages { //сообщения всех контроллеров для ErrorOverlay; меняется на ui-потоке
    private val _list = mutableStateListOf<GlobalMessage>()
    val list: List<GlobalMessage> get() = _list

    fun collectFrom(vararg controllers: UiController) = controllers.forEach { c ->
        c.subscribeMessages { add(it) }
    }

    fun add(mess: GlobalMessage) { _list += mess }

    /** Ошибка, которая уже показана, второй раз не добавляется: исключение в отрисовке повторяется каждый кадр */
    fun addError(title: String, text: String) {
        if (_list.none { it.type == TypeGlobalMessage.ERROR && it.title == title && it.text == text }) {
            add(GlobalMessage.createGlobalError(title, text))
        }
    }

    fun remove(id: Int) { _list.removeIf { it.id == id } }

    fun clear() { _list.clear() }
}