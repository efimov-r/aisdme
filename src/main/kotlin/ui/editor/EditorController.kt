package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.bus.*
import non.era.aisdme.configs.dataclass.snapshots.SnapshotEditor
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.editor.halfviewmodel.HalfViewModelFactory
import non.era.aisdme.ui.editor.halfviewmodel.TabCash
import non.era.aisdme.ui.editor.halfviewmodel.TabCashFile
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import non.era.aisdme.ui.project.CurrentProject
import java.nio.file.Path

enum class Panels{
    LEFT, RIGHT
}

class EditorController(
    private val bus: AppEventBus,
    private val rep: EditorStateRepository,
    private val currentProject: CurrentProject,
) : UiController() {
    private val _tabsCash = mutableStateMapOf<Int, TabCash>() //state: getTab читается в derivedStateOf
    private val removeMutex = Mutex()
    private val checkMutex = Mutex()
    private val tabFactory = HalfViewModelFactory()

    private val updateLeftTabs: (List<Int>) -> Unit = { rep.updateLeftTabs(it); removeClosedTabs() }
    private val updateRightTabs: (List<Int>) -> Unit = { rep.updateRightTabs(it); removeClosedTabs() }
    private val updateLeftActive: (Int?) -> Unit = { rep.updateLeftActiveTabs(it) }
    private val updateRightActive: (Int?) -> Unit = { rep.updateRightActiveTabs(it) }

    var leftTabs by mutableStateOf(HalfEditorTabs(updateLeftTabs, updateLeftActive, ::mergeHalvesLater))
        private set //state: заменяется в rightToLeftHalf
    var rightTabs by mutableStateOf(HalfEditorTabs(updateRightTabs, updateRightActive, ::mergeHalvesLater))
        private set

    init {
        bus.subscribe(scope, Dispatchers.Swing) { event -> //вкладки и _tabsCash меняются только на ui-потоке
            when (event) {
                is FileOpened -> openNodes(listOf(event.node))
                is MoveFile -> {
                    tabsInside(event.oldFile).forEach { tab -> //файл или папка с открытыми файлами внутри
                        val path = tab.getFile().toPath()
                        tab.refile(event.newFile.resolve(event.oldFile.relativize(path)).toFile())
                    }
                    saveToConfig()
                }
                is DeleteFile -> closeTabs(tabsInside(event.file))
                is DeleteDir -> closeTabs(tabsInside(event.dir))
                is RebuilDirRiff -> checkOpenFiles(event.dir.toPath()) //создание и удаление извне: файл мог пропасть
                else -> {}
            }
        }
    }

    fun dropOutsideTree(src: List<ProjectNode>, position: Offset) {
        when {
            leftTabs.rect?.contains(position) == true -> Panels.LEFT
            rightTabs.rect?.contains(position) == true -> Panels.RIGHT
            else -> null
        }?.let { openNodes(src, it) }
    }

    fun dragPosition(position: Offset?) {
        if(position == null) {
            leftTabs.dragDest = false; rightTabs.dragDest = false
            return
        }
        leftTabs.dragDest = (leftTabs.rect?.contains(position) == true)
        rightTabs.dragDest = (rightTabs.rect?.contains(position) == true)
    }

    fun getTab(id: Int) = _tabsCash[id]

    fun closeTab(id: Int) = closeTabs(listOfNotNull(_tabsCash[id])) //из ui: в обеих половинах

    /** Сверить открытые файлы с диском: при возврате фокуса окну и при событии о папке (тогда только её файлы) */
    fun checkOpenFiles(dir: Path? = null) = scope.launch {
        checkMutex.withLock { //по очереди: сверки не должны перекрываться
            val tabs = withContext(Dispatchers.Swing) {
                _tabsCash.values.filterIsInstance<TabCashFile>()
                    .filter { dir == null || it.getFile().toPath().startsWith(dir) }
            }
            val deleted = tabs.filter { it.checkDisk() }
            if (deleted.isNotEmpty()) withContext(Dispatchers.Swing) { closeTabs(deleted) }
        }
    }

    fun showSelectedInTree() {
        when {
            leftTabs.inFocus -> leftTabs.activeTabId
            rightTabs.inFocus -> rightTabs.activeTabId
            else -> null
        }?.let { showInTree(it) }
    }

    fun showInTree(id: Int) {
        val tab = _tabsCash[id]
        if(tab is TabCashFile) bus.publish(tab.showFileInTreeEvent())
    }

    fun reselectProject(snapshot: SnapshotEditor?) { //предпологаем что поток ui

        fun updateHalf(half: HalfEditorTabs, tabs: MutableList<Int>, sel: Int?) {
            var _sel = sel
            tabs.removeIf { !_tabsCash.keys.contains(it) }
            if(!tabs.contains(_sel)) _sel = null
            half.reselectProject(tabs, _sel ?: tabs.lastOrNull())
        }

        _tabsCash.clear()
        tabFactory.clearNexTabId()
        snapshot?.let { snap ->
            val open = snap.leftTabs + snap.rightTabs
            snap.files.forEach { id, snapshotTab ->
                val tabState = tabFactory.createTabFromSnapshot(id, snapshotTab, scope, currentProject)
                    ?: return@forEach
                if(id in open) {
                    _tabsCash[id] = tabState
                    scope.launch {
                        tabState.openFile(TabCash.OpenFileType.OPEN_FROM_CASH)
                        saveToConfig()
                    }
                } else {
                    scope.launch { tabState.beforeRemove() } //закрытая вкладка из старого конфига: удалить её .clip
                }
            }
            updateHalf(leftTabs, snap.leftTabs.toMutableList(), snap.leftTabsSelect)
            updateHalf(rightTabs, snap.rightTabs.toMutableList(), snap.rightTabsSelect)
            mergeHalves()
            scope.launch { saveToConfig() } //без закрытых вкладок
        }
    }

    fun openNodes(nodes: List<ProjectNode>, panel: Panels = Panels.LEFT) {
        fun activateTab(id: Int) = when(panel){
            Panels.RIGHT -> rightTabs.activateTab(id)
            Panels.LEFT -> leftTabs.activateTab(id)
        }

        nodes.forEach { node->
            if(node is FileNode) {
                _tabsCash.values.find { it is TabCashFile && it.compareFile(node.getFile()) }?.id?.let { tabMapId ->
                    activateTab(tabMapId)
                } ?: run {
                    val tab = tabFactory.createFileTab(node.getFile(), scope)
                    tabToCash(tab)
                    activateTab(tab.id)
                }
            }
        }
    }

    fun addFromCpipboardTab() : Int { //создание таба из ui
        val tab = tabFactory.createPasteTab(scope, currentProject)
        tabToCash(tab)
        return tab.id
    }

    private fun tabToCash(tab: TabCash) {
        _tabsCash[tab.id] = tab
        scope.launch {
            tab.openFile(TabCash.OpenFileType.OPEN_NEW_FILE)
            saveToConfig() //после открытия: вкладка с ошибкой в конфиг не попадёт
        }
    }

    private suspend fun saveToConfig() {
        rep.updateFiles( //уходит на сохранение в конфиг: только путь и тип, без текста
            withContext(Dispatchers.Swing) {
                _tabsCash.mapNotNull { (id, tab) ->
                    tab.toSnapshot()?.let { id to it } //вкладка с ошибкой пропускается, остальные сохраняются
                }.toMap()
            }
        )
    }

    private fun tabsInside(path: Path) = _tabsCash.values //путь и всё внутри; по частям: не /a/foobar для /a/foo
        .filterIsInstance<TabCashFile>()
        .filter { it.getFile().toPath().startsWith(path) }

    private fun closeTabs(tabs: List<TabCash>) = tabs.forEach { tab ->
        leftTabs.closeTab(tab.id)
        rightTabs.closeTab(tab.id)
    }

    private fun rightToLeftHalf() {
        leftTabs.detach()
        leftTabs = rightTabs.copy(updateLeftTabs, updateLeftActive)
        rightTabs = HalfEditorTabs(updateRightTabs, updateRightActive, ::mergeHalvesLater)
    }

    private fun mergeHalves() { //левая пустая, а в правой есть вкладки: правая становится левой
        if(leftTabs.listTabs.isEmpty() && rightTabs.listTabs.isNotEmpty()) rightToLeftHalf()
    }

    //после операции целиком: «Split and Move» и closeTabs меняют обе половины по очереди и держат ссылки на них
    private fun mergeHalvesLater() {
        scope.launch(Dispatchers.Swing) { mergeHalves() }
    }

    override fun disposeBefore() {
        runBlocking {
            _tabsCash.values.map { ta ->
                scope.launch {
                    ta.disposeBeforeBlocked()
                }
            }.joinAll()
        }
    }

    internal fun removeClosedTabs() = scope.launch { //вкладки, которых нет ни в одной половине
        removeMutex.withLock { //по очереди: иначе вкладка обрабатывается дважды
            val closed = withContext(Dispatchers.Swing) {
                _tabsCash.values.filter { !isTabOpen(it.id) }
            }
            if(closed.isEmpty()) return@launch

            closed.forEach { tab ->
                tab.beforeRemove() //сохранить текст, удалить .clip
                withContext(Dispatchers.Swing) {
                    if(!isTabOpen(tab.id)) _tabsCash.remove(tab.id) //вкладку могли открыть снова
                }
            }
            saveToConfig()
        }
    }

    private fun isTabOpen(id: Int) = leftTabs.listTabs.contains(id) || rightTabs.listTabs.contains(id)
}