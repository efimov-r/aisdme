package non.era.aisdme.debug

import non.era.aisdme.di.AppController

/**
 * Перетаскивание по именам. Точка — `node имя` (узел дерева), `tap имя` (помеченный элемент) или `x y`:
 * `drag A -> B`, `dragby A dx dy`. Тащится выделенное: перед `drag node …` узел выделить (`node имя`).
 */
class DebugDrag(private val app: AppController) {
    suspend fun between(input: DebugInput, rest: String) {
        val (from, to) = rest.split("->").map { point(it.trim()) }
        input.drag(from.first, from.second, to.first, to.second)
    }

    suspend fun by(input: DebugInput, rest: String) {
        val tokens = rest.split(' ')
        val (dx, dy) = tokens.takeLast(2).map { it.toInt() }
        val (x, y) = point(tokens.dropLast(2).joinToString(" "))
        input.drag(x, y, x + dx, y + dy)
    }

    private fun point(spec: String): Pair<Int, Int> {
        val arg = spec.substringAfter(' ', "")
        return when (spec.substringBefore(' ')) {
            "node" -> DebugState.nodePoint(app, arg).let { (x, y) -> x + DebugState.ROW_DX to y }
            "tap" -> DebugTag.point(arg)
            else -> spec.split(' ').map { it.toInt() }.let { (x, y) -> x to y }
        }
    }
}