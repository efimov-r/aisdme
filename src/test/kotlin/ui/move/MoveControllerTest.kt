package non.era.aisdme.ui.move

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MoveControllerTest {
    @TempDir
    lateinit var dir: File

    private val controller = MoveController()
    private var droppedOnTree: Pair<List<ProjectNode>, File>? = null
    private var droppedOutside: Pair<List<ProjectNode>, Offset>? = null

    @AfterEach
    fun dispose() = controller.dispose()

    private fun file(name: String): ProjectNode =
        FileNode(File(dir, name).apply { writeText("") }, 1)

    private fun folder(name: String): ProjectNode =
        DirNode(File(dir, name).apply { mkdir() }, 1, emptyList())

    /** Узел занимает полосу высотой 20 с отступом [top] */
    private fun place(node: ProjectNode, top: Float) = controller.setNodeBounds(node, Rect(0f, top, 100f, top + 20f))

    private fun finish() = controller.finishDrop(
        resultDropOnTree = { src, target -> droppedOnTree = src to target },
        resultDropOutsideTree = { src, position -> droppedOutside = src to position },
    )

    // у края списка, который некуда прокручивать, автоскролл останавливается сам (#3)
    @Test
    fun autoScrollStopsAtEndOfList() {
        controller.setScrollState(LazyListState())
        controller.setListBounds(Rect(0f, 0f, 100f, 300f))
        controller.onDragStart(Offset(50f, 150f), listOf())

        controller.setDragPosition(Offset(50f, 290f))

        runBlocking {
            withTimeout(5000) {
                while (controller.isAutoScrolling()) delay(10)
            }
        }
    }

    // в середине списка автоскролл не включается
    @Test
    fun middleOfListDoesNotAutoScroll() {
        controller.setScrollState(LazyListState())
        controller.setListBounds(Rect(0f, 0f, 100f, 300f))
        controller.onDragStart(Offset(50f, 150f), listOf())

        controller.setDragPosition(Offset(50f, 150f))

        assertFalse(controller.isAutoScrolling())
    }

    // позиция перетаскивания известна только пока идёт перетаскивание
    @Test
    fun dragPositionIsKnownOnlyWhileDragging() {
        val a = file("a.asm")
        assertNull(controller.getDragPosition())

        controller.onDragStart(Offset(10f, 10f), listOf(a))
        controller.setDragPosition(Offset(20f, 30f))

        assertEquals(Offset(20f, 30f), controller.getDragPosition())
        assertEquals(listOf(a), controller.getNodesDragable())

        controller.cancelDrag()
        assertNull(controller.getDragPosition())
    }

    // целью становится узел под курсором и меняется вместе с курсором
    @Test
    fun dropTargetFollowsCursor() {
        val a = file("a.asm")
        val b = file("b.asm")
        place(a, 0f)
        place(b, 20f)

        controller.onDragStart(Offset(10f, 30f), listOf(a))
        assertTrue(controller.isDropTarget(b))
        assertFalse(controller.isDropTarget(a))

        controller.setDragPosition(Offset(10f, 10f))
        assertTrue(controller.isDropTarget(a))
        assertFalse(controller.isDropTarget(b))

        controller.setDragPosition(Offset(10f, 500f))
        assertFalse(controller.isDropTarget(a))
        assertFalse(controller.isDropTarget(b))
    }

    // без перетаскивания целей нет, даже если курсор над узлом
    @Test
    fun noDropTargetWithoutDrag() {
        val a = file("a.asm")

        place(a, 0f)

        assertFalse(controller.isDropTarget(a))
    }

    // узлы поехали под неподвижным курсором (автоскролл): цель пересчитывается
    @Test
    fun dropTargetUpdatesWhenNodesMove() {
        val a = file("a.asm")
        val b = file("b.asm")
        place(a, 0f)
        place(b, 20f)
        controller.onDragStart(Offset(10f, 10f), listOf())
        assertTrue(controller.isDropTarget(a))

        place(a, 100f)
        place(b, 0f)

        assertTrue(controller.isDropTarget(b))
        assertFalse(controller.isDropTarget(a))
    }

    // узел ушёл с экрана: если он был целью, цели больше нет
    @Test
    fun disposedElementIsNoLongerTarget() {
        val a = file("a.asm")
        place(a, 0f)
        controller.onDragStart(Offset(10f, 10f), listOf())
        assertTrue(controller.isDropTarget(a))

        controller.disposeElement(a)

        assertFalse(controller.isDropTarget(a))
    }

    // отмена сбрасывает перетаскивание и цель
    @Test
    fun cancelDragResetsTarget() {
        val a = file("a.asm")
        place(a, 0f)
        controller.onDragStart(Offset(10f, 10f), listOf(a))

        controller.cancelDrag()

        assertFalse(controller.isDropTarget(a))
        assertNull(controller.getDragPosition())
    }

    // принудительное завершение сбрасывает перетаскивание и цель
    @Test
    fun forceFinishResetsTarget() {
        val a = file("a.asm")
        place(a, 0f)
        controller.onDragStart(Offset(10f, 10f), listOf(a))

        controller.forceFinish()

        assertFalse(controller.isDropTarget(a))
        assertNull(controller.getDragPosition())
    }

    // бросок на папку: узлы переносятся в неё
    @Test
    fun finishDropOnDirDropsIntoIt() {
        val a = file("a.asm")
        val target = folder("src")
        place(target, 0f)
        controller.onDragStart(Offset(10f, 10f), listOf(a))

        finish()

        assertEquals(listOf(a) to target.getFile(), droppedOnTree)
        assertNull(droppedOutside)
        assertNull(controller.getDragPosition())
        assertFalse(controller.isDropTarget(target))
    }

    // бросок на файл: узлы переносятся в папку этого файла
    @Test
    fun finishDropOnFileDropsIntoItsParent() {
        val a = file("a.asm")
        val b = file("b.asm")
        place(b, 0f)
        controller.onDragStart(Offset(10f, 10f), listOf(a))

        finish()

        assertEquals(listOf(a) to dir, droppedOnTree)
    }

    // бросок на один из переносимых узлов ничего не делает
    @Test
    fun finishDropOnDraggedNodeDoesNothing() {
        val a = file("a.asm")
        val b = file("b.asm")
        place(a, 0f)
        controller.onDragStart(Offset(10f, 10f), listOf(a, b))

        finish()

        assertNull(droppedOnTree)
        assertNull(droppedOutside)
    }

    // бросок мимо дерева: узлы и позиция уходят в обработчик «вне дерева»
    @Test
    fun finishDropOutsideTreeReportsPosition() {
        val a = file("a.asm")
        place(file("b.asm"), 0f)
        controller.onDragStart(Offset(10f, 10f), listOf(a))
        controller.setDragPosition(Offset(500f, 500f))

        finish()

        assertNull(droppedOnTree)
        assertEquals(listOf(a) to Offset(500f, 500f), droppedOutside)
    }

    // без начатого перетаскивания бросок ничего не делает
    @Test
    fun finishDropWithoutDragDoesNothing() {
        place(file("a.asm"), 0f)

        finish()

        assertNull(droppedOnTree)
        assertNull(droppedOutside)
    }
}