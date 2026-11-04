package non.era.aisdme.ui.editor.halfviewmodel

import androidx.compose.ui.graphics.Color

enum class WriteStatus(val text: String, val back: Color, val te: Color, val toConfig: Boolean, val readOnly: Boolean) {
    LOAD("load", Color(0xFFaca700), Color.Black, true, true),
    NOT_SAVE("save not", Color(0xFFffc908), Color.Black, true, false),
    WRITABLE("writable", Color(0xFF06c000), Color.Black, true, false),
    READ_ONLY_FS("read only", Color(0xFFffc908), Color.Black, true, true), //!Files.isWritable(getFilepath())
    LOCKED("locked", Color(0xFFf38100), Color.Black, false, true),//error write
    ERROR("error", Color(0xFFe20300), Color.Black, false, true) //!file.exists()
}