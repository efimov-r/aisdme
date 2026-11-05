package non.era.aisdme.files.work

import non.era.aisdme.platform.Platform
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.SystemFlavorMap
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import java.io.File

class FileTransferable(
    private val files: List<File>,
    private val cut: Boolean
) : Transferable {

    override fun getTransferDataFlavors(): Array<DataFlavor> {
        val flavors = mutableListOf(
            DataFlavor.javaFileListFlavor,
            uriFlavor
        )

        if (cut) flavors.add(cutFlavor)

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

            cutFlavor -> ByteArrayInputStream(Platform.current.clipboardCutBytes())

            else -> throw UnsupportedFlavorException(flavor)
        }
    }

    companion object {
        val uriFlavor = DataFlavor("text/uri-list;class=java.lang.String")

        val cutFlavor = DataFlavor("application/x-aisdme-cut;class=java.io.InputStream").also {
            val map = SystemFlavorMap.getDefaultFlavorMap() as SystemFlavorMap
            map.addUnencodedNativeForFlavor(it, Platform.current.clipboardCutNative) //байты пишутся в буфер как есть, без обёртки Java
            map.addFlavorForUnencodedNative(Platform.current.clipboardCutNative, it)
        }
    }
}