package non.era.aisdme.debug

import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.delay

private const val POLL_MS = 50L
private const val EPS = 1f //пиксели: округление границ

/**
 * Проверки раскладки по меткам (`DebugTag`): `expect [not] inside A in B` — A целиком в границах B,
 * `expect [not] apart A and B` — A и B не пересекаются. `waitfor` — с `мс` после слова: `waitfor inside 2000 A in B`.
 * `not` — успех, если условие не выполнено, в том числе когда элемента нет в композиции.
 */
object DebugGeometry {
    private val kinds = mapOf(
        "inside" to (" in " to ::insideProblem),
        "apart" to (" and " to ::apartProblem),
    )

    fun isCheck(word: String) = word in kinds

    suspend fun verify(kind: String, rest: String, withTimeout: Boolean, negate: Boolean): String {
        val (separator, problemOf) = kinds.getValue(kind)
        val timeout = if (withTimeout) rest.substringBefore(' ').toLong() else 0L
        val names = (if (withTimeout) rest.substringAfter(' ') else rest).split(separator, limit = 2)
        require(names.size == 2) { "нужно «A$separator B»" }
        val start = System.currentTimeMillis()

        while (true) {
            //элемента может не быть в композиции (вне экрана): ждём дальше
            val problem = runCatching {
                problemOf(names[0], DebugTag.rect(names[0]), names[1], DebugTag.rect(names[1]))
            }.getOrElse { it.message.orEmpty() }
            if ((problem == null) != negate) return "ok (${System.currentTimeMillis() - start} мс)"
            if (System.currentTimeMillis() - start >= timeout) error(problem ?: "«${names[0]}» $kind «${names[1]}»")
            delay(POLL_MS)
        }
    }

    private fun insideProblem(inner: String, a: Rect, outer: String, b: Rect): String? {
        val frame = b.inflate(EPS)
        return if (frame.contains(a.topLeft) && frame.contains(a.bottomRight)) null
        else "«$inner» $a выходит за «$outer» $b"
    }

    private fun apartProblem(first: String, a: Rect, second: String, b: Rect): String? =
        if (a.deflate(EPS).overlaps(b.deflate(EPS))) "«$first» $a пересекается с «$second» $b" else null
}
