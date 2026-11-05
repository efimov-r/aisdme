package non.era.aisdme.platform

import java.nio.file.Path

interface IPlatform {
    fun configRoot(): Path //папка, в которой приложения ОС хранят свои конфиги
}