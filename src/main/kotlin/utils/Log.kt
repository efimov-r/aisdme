package non.era.aisdme.utils

import java.text.SimpleDateFormat
import java.util.Date

object Log {
    @Volatile var sink: ((String) -> Unit)? = null //пульт отладки копирует строки в файл

    private fun out(str: String) {
        val line = "[${SimpleDateFormat("HH:mm:ss.SSS").format(Date())}] $str"
        println(line)
        sink?.invoke(line)
    }

    fun i(str: String) = out(str)

    fun e(e: Throwable) = out("Error: ${e.message}\n\n${e.stackTraceToString()}")

    fun e(str: String) = out(str)

    fun d(str: String) = out(str)
}