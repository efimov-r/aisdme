package non.era.aisdme.platform

import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

interface IPlatform {
    fun configRoot(): Path //папка, в которой приложения ОС хранят свои конфиги

    val watchSubtree: Boolean //true — registerWatch на корне ловит события всего поддерева
    fun registerWatch(dir: Path, service: WatchService, kinds: Array<WatchEvent.Kind<*>>): WatchKey
}