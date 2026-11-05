package non.era.aisdme.ui.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.nio.file.Path

class CurrentProject { //корень открытого проекта
    var root by mutableStateOf<Path?>(null) //state читается из любого потока: вне композиции — глобальный снимок
        private set

    fun open(path: Path) { //с ui-потока
        root = path
    }
}