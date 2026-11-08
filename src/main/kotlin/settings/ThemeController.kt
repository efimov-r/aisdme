package non.era.aisdme.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.ui.UiController

class ThemeController(
    startDark: Boolean,
    private val globalStore: ConfigStore<AppConfig>,
) : UiController() {
    var isDarkTheme by mutableStateOf(startDark)
        private set

    fun select(dark: Boolean): Job { //с ui-потока
        isDarkTheme = dark
        return safeLaunch {
            globalStore.update { it.copy(isDarkTheme = isDarkTheme) } //значение на момент записи, не нажатия
        }
    }
}