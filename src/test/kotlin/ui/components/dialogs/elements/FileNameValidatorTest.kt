package non.era.aisdme.ui.components.dialogs.elements

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FileNameValidatorTest {

    // обычное имя файла проходит
    @Test
    fun validNameReturnsNull() {
        assertNull(FileNameValidator.validateFileName("main.asm"))
    }

    // пустое имя и имя из пробелов запрещены
    @Test
    fun blankNameReturnsError() {
        assertNotNull(FileNameValidator.validateFileName(""))
        assertNotNull(FileNameValidator.validateFileName("   "))
    }

    // слэш запрещён на любой ОС
    @Test
    fun slashReturnsError() {
        assertNotNull(FileNameValidator.validateFileName("a/b"))
    }

    // символы, запрещённые в Windows
    @Test
    fun windowsInvalidCharsReturnError() {
        "<>:\"\\|?*".forEach { c ->
            assertNotNull(FileNameValidator.validateFileName("a${c}b"), "символ $c")
        }
    }

    // Windows не даёт имя с пробелом или точкой в конце
    @Test
    fun trailingSpaceOrDotReturnsError() {
        assertNotNull(FileNameValidator.validateFileName("file "))
        assertNotNull(FileNameValidator.validateFileName("file."))
    }

    // зарезервированные имена Windows — в любом регистре
    @Test
    fun windowsReservedNameReturnsError() {
        assertNotNull(FileNameValidator.validateFileName("CON"))
        assertNotNull(FileNameValidator.validateFileName("con"))
        assertNotNull(FileNameValidator.validateFileName("Lpt1"))
    }

    // предел длины имени — 255 символов
    @Test
    fun nameLongerThan255ReturnsError() {
        assertNull(FileNameValidator.validateFileName("a".repeat(255)))
        assertNotNull(FileNameValidator.validateFileName("a".repeat(256)))
    }
}