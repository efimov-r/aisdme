package non.era.aisdme.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Lang(val code: String, val title: String, val strings: Strings) {
    EN("en", "English", StringsEn),
    RU("ru", "Русский", StringsRu);

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull { it.code == code } ?: EN
    }
}

object I18n { //текущий язык; состояние Compose, поэтому экран перерисовывается при смене
    var lang by mutableStateOf(Lang.EN)

    val s: Strings get() = lang.strings
}
