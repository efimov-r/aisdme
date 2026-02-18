package non.era.aisdme.utils

import java.text.SimpleDateFormat
import java.util.Date

object Log {
    fun i(str: String) {
        println("[${SimpleDateFormat("HH:mm:ss.SSS").format(Date())}] $str")
    }

    fun e(e: Throwable) {
        val stackTrace = e.stackTraceToString()
        println("[${SimpleDateFormat("HH:mm:ss.SSS").format(Date())}] Error: ${e.message}\n\n$stackTrace")
    }

    fun e(str: String) {
        println("[${SimpleDateFormat("HH:mm:ss.SSS").format(Date())}] $str")
    }

    fun d(str: String) {
        println("[${SimpleDateFormat("HH:mm:ss.SSS").format(Date())}] $str")
    }
}