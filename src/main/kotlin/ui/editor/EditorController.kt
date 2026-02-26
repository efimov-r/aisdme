package non.era.aisdme.ui.editor

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.BuildConfig
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.FileOpened
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File

enum class Panels{
    LEFT, RIGHT
}

class EditorController(
    private val bus: AppEventBus
) : UiController() {
    private var files: MutableMap<Int, ObservableOpenedFile> = mutableMapOf()

    var leftTabs = HalfEditorTabs(files, scope)
    var rightTabs = HalfEditorTabs(files, scope)

    init {
        scope.launch {
            bus.events.collect { event ->
                when (event) {
                    is FileOpened -> openFile(event.file)
                    else -> {}
                }
            }
        }
    }


    fun openNodes(nodes: List<ProjectNode>, panel: Panels = Panels.LEFT) {
        nodes.forEach { node->
            if(node is FileNode) openFile(node.getFile(), panel)
        }
    }

    private fun activateTab(id: Int, panel: Panels) = when(panel){
        Panels.RIGHT -> rightTabs.activateTab(id)
        Panels.LEFT -> leftTabs.activateTab(id)
    }

    private val listMutex: Mutex = Mutex() //todo нннаада?

    private fun openFile(file: File, panel: Panels = Panels.LEFT) {
        files.values.find { it.file == file }?.id?.let { tabMapId ->
            activateTab(tabMapId, panel)
        } ?: run {
            val tabEmpty = ObservableOpenedFile.createEmpty(file = file)
            files[tabEmpty.id] = tabEmpty
            activateTab(tabEmpty.id, panel)
            scope.async {
                runCatching {
                    if(BuildConfig.DEBUG) {
                        delay(300)
                    }
                    val text = if (file.exists()) file.readText() else ""
                    withContext(Dispatchers.Swing) {
                        listMutex.withLock {
                            tabEmpty.initText(text)
                            tabEmpty.readOnly = false
                            tabEmpty.loadState = false
                        }
                    }
                }.getOrElse {
                    withContext(Dispatchers.Swing) {
                        listMutex.withLock {
                            tabEmpty.error = it.message
                        }
                    }
                }
            }
        }
    }

    fun rightToLeftHalf() {
        leftTabs.closeAllTabs()
        leftTabs = rightTabs
        rightTabs = HalfEditorTabs(files, scope)
    }

    override fun disposeBefore() {
        runBlocking {
            files.values.map { ta ->
                scope.launch {
                    ta.autosaveJob?.cancel()
                    if (!ta.file.canWrite()) return@launch
                    runCatching {
                        ta.writeTextToFile()
                    }
                }
            }.joinAll()
        }
    }
}