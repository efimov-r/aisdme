package non.era.aisdme.debug

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skiko.SkiaLayer
import java.awt.Component
import java.awt.Container
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

fun findSkiaLayer(root: Container): SkiaLayer? = root.components.firstNotNullOfOrNull {
    it as? SkiaLayer ?: (it as? Container)?.let(::findSkiaLayer)
}

fun dumpComponents(c: Component, depth: Int = 0): String {
    val line = "${"  ".repeat(depth)}${c.javaClass.name} ${c.bounds} mouse=${c.mouseListeners.size} " +
        "motion=${c.mouseMotionListeners.size} wheel=${c.mouseWheelListeners.size} key=${c.keyListeners.size}\n"
    return line + ((c as? Container)?.components?.joinToString("") { dumpComponents(it, depth + 1) } ?: "")
}

fun crop(png: ByteArray, rect: List<Int>): ByteArray { //x, y, ширина, высота — в пикселях скриншота
    val image = ImageIO.read(ByteArrayInputStream(png))
    val (pos, size) = rect.chunked(2)
    val x = pos[0].coerceAtLeast(0)
    val y = pos[1].coerceAtLeast(0)
    val sub = image.getSubimage(x, y, size[0].coerceAtMost(image.width - x), size[1].coerceAtMost(image.height - y))
    return ByteArrayOutputStream().also { ImageIO.write(sub, "png", it) }.toByteArray()
}

fun SkiaLayer.png(): ByteArray? { //вызывать на ui-потоке
    val bitmap = screenshot() ?: return null
    return Image.makeFromBitmap(bitmap).encodeToData(EncodedImageFormat.PNG)?.bytes
}