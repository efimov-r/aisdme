package non.era.aisdme.files

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TypeFile {
    @SerialName("textFile")
    TEXT_FILE,

    @SerialName("pasteTab")
    PASTE_TAB,

    @SerialName("imgFile")
    IMG_FILE,


}