package non.era.aisdme.settings

import kotlinx.coroutines.runBlocking
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.i18n.I18n
import non.era.aisdme.i18n.Lang
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class LanguageControllerTest {
    @TempDir
    lateinit var dir: File

    private val store by lazy { ConfigStore(File(dir, "global.json").toPath(), AppConfig.serializer()) { AppConfig() } }
    private val controller by lazy { LanguageController(startLang = Lang.EN, globalStore = store) }

    @AfterEach
    fun dispose() {
        controller.dispose()
        I18n.lang = Lang.EN //язык общий на всё приложение, другие тесты ждут английский
    }

    // стартовый язык становится текущим
    @Test
    fun startLangIsApplied() {
        LanguageController(startLang = Lang.RU, globalStore = store).dispose()

        assertEquals(Lang.RU, I18n.lang)
    }

    // выбор языка меняет тексты и пишет код языка в глобальный конфиг
    @Test
    fun selectSavesLanguage() {
        runBlocking { controller.select(Lang.RU).join() }

        assertEquals(Lang.RU, controller.lang)
        assertEquals("Отмена", I18n.s.cancel)
        assertEquals("ru", runBlocking { store.get() }.language)
    }

    // два выбора подряд: в конфиге то же, что на экране
    @Test
    fun doubleSelectSavesLastValue() {
        val first = controller.select(Lang.RU)
        val second = controller.select(Lang.EN)
        runBlocking { first.join(); second.join() }

        assertEquals(Lang.EN, controller.lang)
        assertEquals("en", runBlocking { store.get() }.language)
    }

    // неизвестный или пустой код из конфига — английский
    @Test
    fun unknownCodeFallsBackToEnglish() {
        assertEquals(Lang.EN, Lang.fromCode(null))
        assertEquals(Lang.EN, Lang.fromCode("de"))
        assertEquals(Lang.RU, Lang.fromCode("ru"))
    }
}
