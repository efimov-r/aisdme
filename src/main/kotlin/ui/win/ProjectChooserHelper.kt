package non.era.aisdme.ui.win

import non.era.aisdme.utils.Log
import java.awt.Component
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager

object ProjectChooserHelper {

    /** Выбор папки проекта. null — отмена или выбрана не папка */
    fun chooseProjectDir(parent: Component?): File? { //вызывать из потока ui
        val chooser = createChooser()
        val res = chooser.showOpenDialog(parent)
        if (res != JFileChooser.APPROVE_OPTION) return null

        return chooser.selectedFile?.takeIf { it.isDirectory }
    }

    private fun createChooser(): JFileChooser {
        // системный вид только на время создания диалога, остальной Swing не трогаем
        val oldLaf = UIManager.getLookAndFeel()
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: Exception) {
            Log.e(e)
        }

        val res = JFileChooser().apply {
            dialogTitle = "Открыть проект"
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isAcceptAllFileFilterUsed = false
            currentDirectory = File(System.getProperty("user.home"))
        }

        try {
            UIManager.setLookAndFeel(oldLaf)
        } catch (e: Exception) {
            Log.e(e)
        }
        return res
    }
}