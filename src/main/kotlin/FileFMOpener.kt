package non.era.aisdme

import java.awt.Desktop
import java.io.File
import java.io.IOException

object FileFMOpener {
    fun openFileInFileManager(file: File) {

        // Проверяем, существует ли файл/папка
        if (!file.exists()) {
            println("Файл не существует: $file")
            return
        }

        when {
            // Используем Desktop API если доступно (Java)
            Desktop.isDesktopSupported() -> {
                try {
                    val desktop = Desktop.getDesktop()
                    if (file.isDirectory) {
                        desktop.open(file) // Открывает папку в файловом менеджере
                    } else {
                        // Для файла показываем содержащую папку и выделяем файл
                        desktop.open(file.parentFile)
                    }
                } catch (e: IOException) {
                    println("Не удалось открыть через Desktop API: ${e.message}")
                    openWithSystemCommand(file)
                }
            }

            // Linux с определением файлового менеджера
            System.getProperty("os.name").contains("linux", ignoreCase = true) -> {
                openInLinux(file)
            }

            else -> {
                openWithSystemCommand(file)
            }
        }
    }

    private fun openInLinux(file: File) {
        val manager = detectLinuxFileManager()

        val commands = when (manager) {
            "dolphin" -> arrayOf("dolphin", "--select", file.absolutePath)
            "nautilus" -> arrayOf("nautilus", file.absolutePath)
            "thunar" -> arrayOf("thunar", file.absolutePath)
            "pcmanfm" -> arrayOf("pcmanfm", file.absolutePath)
            else -> arrayOf("xdg-open", file.parent ?: file.absolutePath)
        }

        try {
            ProcessBuilder(*commands).start()
        } catch (e: IOException) {
            println("Не удалось открыть в $manager: ${e.message}")
            // Пробуем xdg-open как запасной вариант
            try {
                ProcessBuilder("xdg-open", file.parent ?: file.absolutePath).start()
            } catch (e2: IOException) {
                println("Не удалось открыть через xdg-open: ${e2.message}")
            }
        }
    }

    private fun detectLinuxFileManager(): String {
        // Проверяем, какой файловый менеджер установлен
        val fileManagers = listOf("dolphin", "nautilus", "thunar", "pcmanfm")

        return fileManagers.firstOrNull { manager ->
            try {
                ProcessBuilder("which", manager).start().waitFor() == 0
            } catch (e: Exception) {
                false
            }
        } ?: "xdg-open"
    }

    private fun openWithSystemCommand(file: File) {
        val os = System.getProperty("os.name").lowercase()

        val command = when {
            os.contains("win") -> {
                if (file.isDirectory) {
                    arrayOf("explorer", file.absolutePath)
                } else {
                    arrayOf("explorer", "/select,", file.absolutePath)
                }
            }
            os.contains("mac") -> {
                arrayOf("open", "-R", file.absolutePath)
            }
            else -> { // Linux/Unix
                arrayOf("xdg-open", file.parent ?: file.absolutePath)
            }
        }

        try {
            ProcessBuilder(*command).start()
        } catch (e: IOException) {
            println("Не удалось выполнить команду ${command.joinToString(" ")}: ${e.message}")
        }
    }

    /**
     * Открыть папку, содержащую файл, с выделением файла
     */
    fun openContainingFolder(filePath: String) {
        val file = File(filePath)

        if (!file.exists()) {
            println("Файл не существует: $filePath")
            return
        }

        when (System.getProperty("os.name").lowercase()) {
            "linux" -> {
                // Для Dolphin
                try {
                    ProcessBuilder("dolphin", "--select", file.absolutePath).start()
                } catch (e: IOException) {
                    // Пробуем другие способы
                    try {
                        Desktop.getDesktop().open(file.parentFile)
                    } catch (e2: IOException) {
                        ProcessBuilder("xdg-open", file.parent).start()
                    }
                }
            }
            "mac os x" -> {
                ProcessBuilder("open", "-R", file.absolutePath).start()
            }
            else -> { // Windows
                ProcessBuilder("explorer", "/select,", file.absolutePath).start()
            }
        }
    }
}