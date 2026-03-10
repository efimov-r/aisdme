package non.era.aisdme.utils

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

object ClipboardHelper {
    fun clipboardHasText(): Boolean {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        return clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)
    }

    fun getClipboardText(): String? {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard

        return if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            clipboard.getData(DataFlavor.stringFlavor) as? String
        } else null
    }
}