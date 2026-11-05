package non.era.aisdme.ui.project

import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectListLineHelperTest {
    private val a = FileNode(File("root/a"), 1)
    private val b = FileNode(File("root/b"), 1)
    private val c = FileNode(File("root/c"), 1)
    private val d = FileNode(File("root/d"), 1)
    private val nodes = listOf(DirNode(File("root"), 0, listOf(a, b, c, d)))
    private val selection = TreeSelection()

    private fun select(vararg list: ProjectNode) = list.forEach { selection.set(it, true) }

    private fun selected() = ProjectTreeHelper.findSelectedNodes(nodes, selection)

    // Shift+клик выделяет диапазон от выделенного до узла
    @Test
    fun fromAndToSelectsRange() {
        select(b)

        ProjectListLineHelper.fromAndTo(nodes, selected(), d, selection)

        assertEquals(listOf(b, c, d), selected())
    }

    // Shift+клик без выделения выделяет только узел
    @Test
    fun fromAndToWithoutSelectionSelectsOnlyTarget() {
        ProjectListLineHelper.fromAndTo(nodes, selected(), c, selection)

        assertEquals(listOf(c), selected())
    }

    // Shift+вниз от одного узла добавляет следующий
    @Test
    fun shiftBottomFromSingleAddsNext() {
        select(b)

        ProjectListLineHelper.shiftBottom(nodes, selected(), Movement.BOTTOM, selection)

        assertEquals(listOf(b, c), selected())
    }

    // Shift+вниз после движения вверх снимает верхний узел
    @Test
    fun shiftBottomAfterTopRemovesFirst() {
        select(b, c)

        ProjectListLineHelper.shiftBottom(nodes, selected(), Movement.TOP, selection)

        assertEquals(listOf(c), selected())
    }

    // Shift+вверх после движения вверх добавляет узел выше
    @Test
    fun shiftTopAfterTopAddsPrevious() {
        select(b, c)

        ProjectListLineHelper.shiftTop(nodes, selected(), Movement.TOP, selection)

        assertEquals(listOf(a, b, c), selected())
    }

    // Shift+вверх после движения вниз снимает нижний узел
    @Test
    fun shiftTopAfterBottomRemovesLast() {
        select(b, c)

        ProjectListLineHelper.shiftTop(nodes, selected(), Movement.BOTTOM, selection)

        assertEquals(listOf(b), selected())
    }
}