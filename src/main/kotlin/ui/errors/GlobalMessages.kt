package non.era.aisdme.ui.errors

import androidx.compose.runtime.mutableStateListOf
import non.era.aisdme.ui.UiController

class GlobalMessages { //сообщения всех контроллеров для ErrorOverlay; меняется на ui-потоке
    private val _list = mutableStateListOf<GlobalMessage>()
    val list: List<GlobalMessage> get() = _list

    fun collectFrom(vararg controllers: UiController) = controllers.forEach { c ->
        c.subscriptionError { add(it) }
    }

    fun add(mess: GlobalMessage) { _list += mess }

    fun remove(id: Int) { _list.removeIf { it.id == id } }

    fun clear() { _list.clear() }
}