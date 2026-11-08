package non.era.aisdme.utils

object TextHelper {
    private const val ELLIPSIS = "…"
    private const val TAIL_SHARE = 2 //хвосту (имени файла) — две трети оставшихся символов
    private const val SHARES = 3

    /** Текст, сокращённый из середины многоточием, пока он не влезет (`fits`). Конец сохраняется лучше начала */
    fun middleEllipsis(text: String, fits: (String) -> Boolean): String {
        if (fits(text)) return text

        fun cut(keep: Int): String {
            val tail = keep * TAIL_SHARE / SHARES
            return text.take(keep - tail) + ELLIPSIS + text.takeLast(tail)
        }

        var low = 0 //влезает всегда: одно многоточие
        var high = text.length - 1
        while (low < high) {
            val mid = (low + high + 1) / 2
            if (fits(cut(mid))) low = mid else high = mid - 1
        }
        return cut(low)
    }
}
