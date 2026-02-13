package non.era.aisdme.files

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.File

class FileTransferable(
    private val files: List<File>,
    private val cut: Boolean
) : Transferable {

    private val uriFlavor = DataFlavor("text/uri-list;class=java.lang.String")
    private val cutFlavor = DataFlavor("application/x-kde-cutselection;class=java.lang.String")

    override fun getTransferDataFlavors(): Array<DataFlavor> {
        val flavors = mutableListOf(
            DataFlavor.javaFileListFlavor,
            uriFlavor
        )

        if (cut) {
            flavors.add(cutFlavor)   // ← ТОЛЬКО если cut!
        }

        return flavors.toTypedArray()
    }

    override fun isDataFlavorSupported(flavor: DataFlavor) =
        getTransferDataFlavors().contains(flavor)

    override fun getTransferData(flavor: DataFlavor): Any {
        return when (flavor) {
            DataFlavor.javaFileListFlavor -> files

            uriFlavor -> files.joinToString("\r\n") {
                it.toURI().toString()
            }

            cutFlavor -> "1"

            else -> throw UnsupportedFlavorException(flavor)
        }
    }
}