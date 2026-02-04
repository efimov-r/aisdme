package non.era.aisdme.ui.editor

import java.io.File

data class EditorTab(
    val id: Int,
    val file: File?,
    val text: String,
    val savedText: String,
    val error: String? = null
) {
    val isModified: Boolean
        get() = text != savedText //todo &&&&

    val title: String
        get() = (file?.name ?: "Untitled") + if (isModified) "*" else ""
}