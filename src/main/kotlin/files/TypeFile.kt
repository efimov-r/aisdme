package non.era.aisdme.files

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.nio.file.Files
import java.nio.file.Path

@Serializable
enum class TypeFile {
    @SerialName("textFile")
    TEXT_FILE,

    @SerialName("pasteTab")
    PASTE_TAB,

    @SerialName("imgFile")
    IMG_FILE;

    companion object {
        fun isImageFile(path: Path): Boolean {

            val ext = path.fileName.toString()
                .substringAfterLast('.', "")
                .lowercase()

            val imageExt = setOf(
                "png","jpg","jpeg","gif","bmp","webp","tif","tiff"
            )

            if (ext in imageExt) return true

            val mime = Files.probeContentType(path)

            return mime?.startsWith("image/") == true
        }

    }
}

