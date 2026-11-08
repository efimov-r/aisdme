package non.era.aisdme.ui.components.dialogs.elements

import non.era.aisdme.i18n.I18n

object FileNameValidator {
    private val windowsReserved = setOf(
        "CON","PRN","AUX","NUL",
        "COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8","COM9",
        "LPT1","LPT2","LPT3","LPT4","LPT5","LPT6","LPT7","LPT8","LPT9"
    )

    fun validateFileName(name: String): String? {
        val s = I18n.s
        if (name.isBlank()) return s.nameEmpty

        if (name.contains('/'))
            return s.nameHasSlash

        val winInvalid = Regex("""[<>:"\\|?*]""")
        if (winInvalid.containsMatchIn(name))
            return s.nameInvalidChars

        if (name.last() == ' ' || name.last() == '.')
            return s.nameBadEnd

        if (windowsReserved.contains(name.uppercase()))
            return s.nameReserved

        if (name.length > 255)
            return s.nameTooLong

        return null
    }
}