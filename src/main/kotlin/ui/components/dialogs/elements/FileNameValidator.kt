package non.era.aisdme.ui.components.dialogs.elements

object FileNameValidator {
    private val windowsReserved = setOf(
        "CON","PRN","AUX","NUL",
        "COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8","COM9",
        "LPT1","LPT2","LPT3","LPT4","LPT5","LPT6","LPT7","LPT8","LPT9"
    )

    fun validateFileName(name: String): String? {
        if (name.isBlank()) return "The name cannot be empty"

        if (name.contains('/'))
            return "The name must not contain /"

        val winInvalid = Regex("""[<>:"\\|?*]""")
        if (winInvalid.containsMatchIn(name))
            return "Invalid characters: <>:\"\\|?*"

        if (name.last() == ' ' || name.last() == '.')
            return "The name must not end with a space or dot."

        if (windowsReserved.contains(name.uppercase()))
            return "Windows reserved"

        if (name.length > 255)
            return "The name is too long"

        return null
    }
}