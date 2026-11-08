package non.era.aisdme.settings

import kotlinx.coroutines.Job
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.i18n.I18n
import non.era.aisdme.i18n.Lang
import non.era.aisdme.ui.UiController

class LanguageController(
    startLang: Lang,
    private val globalStore: ConfigStore<AppConfig>,
) : UiController() {
    init {
        I18n.lang = startLang
    }

    val lang get() = I18n.lang

    fun select(lang: Lang): Job { //с ui-потока
        I18n.lang = lang
        return safeLaunch {
            globalStore.update { it.copy(language = I18n.lang.code) } //значение на момент записи, не нажатия
        }
    }
}
