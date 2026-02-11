package non.era.aisdme

object Log {
    fun i(str: String) {
        println(str)
    }

    fun e(e: Throwable) {
        println("Error ${e.message}")
    }

    fun e(str: String) {
        println(str)
    }

    fun d(str: String) {
        println(str)
    }
}