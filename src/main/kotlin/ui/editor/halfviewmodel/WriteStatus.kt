package non.era.aisdme.ui.editor.halfviewmodel

import androidx.compose.ui.graphics.Color
import non.era.aisdme.i18n.I18n
import non.era.aisdme.theme.AisDmeColors

enum class WriteStatus(val text: String, val toConfig: Boolean, val readOnly: Boolean) {
    LOAD("load", true, true),
    NOT_SAVE("save not", true, false),
    WRITABLE("writable", true, false),
    READ_ONLY_FS("read only", true, true), //!Files.isWritable(getFilepath())
    LOCKED("locked", false, true),//error write
    ERROR("error", false, true), //!file.exists()
    CONFLICT("changed", true, true), //изменён снаружи, во вкладке есть ввод
    DELETED("deleted", false, true); //удалён снаружи, во вкладке есть ввод

    val label get() = with(I18n.s) { //для экрана; text — стабильное имя для отладки и конфига
        when (this@WriteStatus) {
            LOAD -> statusLoad
            NOT_SAVE -> statusNotSaved
            WRITABLE -> statusWritable
            READ_ONLY_FS -> statusReadOnly
            LOCKED -> statusLocked
            ERROR -> statusError
            CONFLICT -> statusChanged
            DELETED -> statusDeleted
        }
    }

    fun back(c: AisDmeColors): Color = when (this) { //плашка статуса: цвет из темы
        LOAD -> c.statusLoad
        WRITABLE -> c.statusOk
        NOT_SAVE, READ_ONLY_FS -> c.statusWarn
        LOCKED, CONFLICT -> c.statusAlert
        ERROR, DELETED -> c.statusError
    }

    val awaitsChoice get() = this == CONFLICT || this == DELETED //ввод закрыт, пока пользователь не выберет
}
