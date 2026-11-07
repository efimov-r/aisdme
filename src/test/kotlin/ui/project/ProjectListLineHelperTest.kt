package non.era.aisdme.ui.project

import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectListLineHelperTest {
    private val a = FileNode(File("root/a"), 1)
    private val b = FileNode(File("root/b"), 1)
    private val c = FileNode(File("root/c"), 1)
    private val d = FileNode(File("root/d"), 1)
    private val nodes = listOf(DirNode(File("root"), 0, listOf(a, b, c, d)))
    private val selection = TreeSelection()

    private val e = FileNode(File("root2/sub/e"), 2)
    private val sub = DirNode(File("root2/sub"), 1, listOf(e), expand = false)
    private val x = FileNode(File("root2/x"), 1)
    private val y = FileNode(File("root2/y"), 1)
    private val nested = listOf(DirNode(File("root2"), 0, listOf(x, sub, y))) //sub свёрнута

    private fun select(vararg list: ProjectNode) = list.forEach { selection.set(it, true) }

    private fun selected(tree: List<ProjectNode> = nodes) = ProjectTreeHelper.findSelectedNodes(tree, selection)

    // в линейный список раскрытых узлов не попадает содержимое свёрнутой папки
    @Test
    fun linearExpandedSkipsCollapsedChildren() {
        assertEquals(listOf(nested[0], x, sub, y), ProjectListLineHelper.buildLinearExpandedNodes(nested))
        assertEquals(listOf(nested[0], x, sub, e, y), ProjectListLineHelper.buildLinearNodes(nested))
    }

    // вниз — выделение переходит на следующий узел после последнего выделенного
    @Test
    fun nextMovesFromLastSelected() {
        select(b, c)

        ProjectListLineHelper.next(nodes, selected(), selection)

        assertEquals(listOf(d), selected())
    }

    // вниз на последнем узле — выделение остаётся
    @Test
    fun nextOnLastNodeStays() {
        select(d)

        ProjectListLineHelper.next(nodes, selected(), selection)

        assertEquals(listOf(d), selected())
    }

    // вниз со свёрнутой папки — её содержимое пропускается
    @Test
    fun nextSkipsCollapsedDir() {
        select(sub)

        ProjectListLineHelper.next(nested, selected(nested), selection)

        assertEquals(listOf(y), selected(nested))
    }

    // вниз с раскрытой папки — на её первый узел
    @Test
    fun nextEntersExpandedDir() {
        sub.expanded = true
        select(sub)

        ProjectListLineHelper.next(nested, selected(nested), selection)

        assertEquals(listOf(e), selected(nested))
    }

    // вверх — выделение переходит на узел перед первым выделенным
    @Test
    fun lastMovesFromFirstSelected() {
        select(b, c)

        ProjectListLineHelper.last(nodes, selected(), selection)

        assertEquals(listOf(a), selected())
    }

    // вверх на первом узле — выделение остаётся
    @Test
    fun lastOnFirstNodeStays() {
        select(nodes[0])

        ProjectListLineHelper.last(nodes, selected(), selection)

        assertEquals(listOf(nodes[0]), selected())
    }

    // вниз при пустом выделении — верхний узел, вверх — нижний из видимых (содержимое свёрнутой папки не считается)
    @Test
    fun emptySelectionGoesToTopOrBottomNode() {
        ProjectListLineHelper.next(nested, selected(nested), selection)
        assertEquals(listOf(nested[0]), selected(nested))

        selection.clear()
        ProjectListLineHelper.last(nested, selected(nested), selection)
        assertEquals(listOf(y), selected(nested))
    }

    // пустое дерево и пустое выделение: ничего не происходит
    @Test
    fun emptyTreeAndSelectionDoNothing() {
        ProjectListLineHelper.next(emptyList(), emptyList(), selection)
        ProjectListLineHelper.last(emptyList(), emptyList(), selection)

        assertTrue(selection.isEmpty())
    }

    // Shift+клик выше выделенного выделяет диапазон вверх
    @Test
    fun fromAndToSelectsRangeUpward() {
        select(d)

        ProjectListLineHelper.fromAndTo(nodes, selected(), b, selection)

        assertEquals(listOf(b, c, d), selected())
    }

    // Shift+клик по узлу в свёрнутой папке ничего не меняет
    @Test
    fun fromAndToHiddenTargetIgnored() {
        select(x)

        ProjectListLineHelper.fromAndTo(nested, selected(nested), e, selection)

        assertEquals(listOf(x), selected(nested))
    }

    // Shift+вверх от одного узла добавляет предыдущий
    @Test
    fun shiftTopFromSingleAddsPrevious() {
        select(c)

        ProjectListLineHelper.shiftTop(nodes, selected(), null, selection)

        assertEquals(listOf(b, c), selected())
    }

    // Shift+вниз на последнем узле — выделение не меняется
    @Test
    fun shiftBottomOnLastNodeKeepsSelection() {
        select(d)

        ProjectListLineHelper.shiftBottom(nodes, selected(), null, selection)

        assertEquals(listOf(d), selected())
    }

    // Shift+вниз после движения вниз добавляет узел ниже
    @Test
    fun shiftBottomAfterBottomAddsNext() {
        select(b, c)

        ProjectListLineHelper.shiftBottom(nodes, selected(), Movement.BOTTOM, selection)

        assertEquals(listOf(b, c, d), selected())
    }

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