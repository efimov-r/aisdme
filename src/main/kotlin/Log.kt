package non.era.aisdme

object Log {
    fun i(str: String) {
        println(str)
    }

    fun e(e: Throwable) {
        val stackTrace = e.stackTraceToString()
        println("Error: ${e.message}\n\n${stackTrace}")
    }

    fun e(str: String) {
        println(str)
    }

    fun d(str: String) {
        println(str)
    }
}