package non.era.aisdme.debug

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skiko.SkiaLayer
import java.awt.Component
import java.awt.Container

fun findSkiaLayer(root: Container): SkiaLayer? = root.components.firstNotNullOfOrNull {
    it as? SkiaLayer ?: (it as? Container)?.let(::findSkiaLayer)
}

fun dumpComponents(c: Component, depth: Int = 0): String {
    val line = "${"  ".repeat(depth)}${c.javaClass.name} ${c.bounds} mouse=${c.mouseListeners.size} " +
        "motion=${c.mouseMotionListeners.size} wheel=${c.mouseWheelListeners.size} key=${c.keyListeners.size}\n"
    return line + ((c as? Container)?.components?.joinToString("") { dumpComponents(it, depth + 1) } ?: "")
}

fun SkiaLayer.png(): ByteArray? { //вызывать на ui-потоке
    val bitmap = screenshot() ?: return null
    return Image.makeFromBitmap(bitmap).encodeToData(EncodedImageFormat.PNG)?.bytes
}