package non.era.aisdme.files.work

import non.era.aisdme.platform.Platform
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.File
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileTransferableTest {
    private val files = listOf(File("a.asm"), File("src/b.asm"))

    // копирование предлагает список файлов и uri, но не пометку «вырезано»
    @Test
    fun copyOffersFilesAndUriWithoutCutFlavor() {
        val transferable = FileTransferable(files, false)

        assertContentEquals(
            arrayOf(DataFlavor.javaFileListFlavor, FileTransferable.uriFlavor),
            transferable.transferDataFlavors,
        )
        assertTrue(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        assertTrue(transferable.isDataFlavorSupported(FileTransferable.uriFlavor))
        assertFalse(transferable.isDataFlavorSupported(FileTransferable.cutFlavor))
    }

    // вырезание добавляет пометку «вырезано»
    @Test
    fun cutAddsCutFlavor() {
        val transferable = FileTransferable(files, true)

        assertTrue(transferable.isDataFlavorSupported(FileTransferable.cutFlavor))
        assertEquals(3, transferable.transferDataFlavors.size)
    }

    // список файлов отдаётся как есть
    @Test
    fun fileListFlavorReturnsFiles() {
        val data = FileTransferable(files, false).getTransferData(DataFlavor.javaFileListFlavor)

        assertEquals(files, data)
    }

    // uri-list: по строке на файл, разделитель — CRLF
    @Test
    fun uriFlavorReturnsOneUriPerLine() {
        val data = FileTransferable(files, false).getTransferData(FileTransferable.uriFlavor) as String

        assertEquals(files.map { it.toURI().toString() }, data.split("\r\n"))
        assertTrue(data.startsWith("file:"))
    }

    // пометка «вырезано» читается платформой как вырезание
    @Test
    fun cutFlavorBytesAreRecognizedAsCut() {
        val data = FileTransferable(files, true).getTransferData(FileTransferable.cutFlavor) as InputStream

        assertTrue(Platform.current.isClipboardCut(data.readBytes()))
    }

    // неизвестный формат — UnsupportedFlavorException
    @Test
    fun unknownFlavorIsUnsupported() {
        assertFailsWith<UnsupportedFlavorException> {
            FileTransferable(files, false).getTransferData(DataFlavor.stringFlavor)
        }
    }
}