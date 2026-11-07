package non.era.aisdme.debug

import non.era.aisdme.utils.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintStream

/** Копия лога приложения в `app.log` рядом с командами: читать без доступа к выводу `gradlew run`. */
object DebugLog {
    private val lock = Any()
    private var file: File? = null

    fun attach(dir: File) {
        if (file != null) return //main() может выполниться повторно при перекомпозиции
        val f = File(dir, "app.log").also { it.writeText("") }
        file = f
        Log.sink = { line -> synchronized(lock) { f.appendText(line + "\n") } }
        System.setErr(PrintStream(TeeStream(System.err, FileOutputStream(f, true)), true)) //исключения идут в stderr
    }

    fun all(): String = synchronized(lock) { file?.readText().orEmpty() }

    fun tail(n: Int): String = all().lines().takeLast(n).joinToString("\n")
}

private class TeeStream(private val first: OutputStream, private val second: OutputStream) : OutputStream() {
    override fun write(b: Int) {
        first.write(b)
        second.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        first.write(b, off, len)
        second.write(b, off, len)
    }

    override fun flush() {
        first.flush()
        second.flush()
    }
}