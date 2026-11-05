package non.era.aisdme.ui.editor.halfviewmodel

import kotlinx.coroutines.CoroutineScope
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash
import non.era.aisdme.files.TypeFile
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileImg
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashPaste
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText
import non.era.aisdme.ui.project.CurrentProject
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class HalfViewModelFactory { //своя у каждого EditorController: нумерация вкладок не общая
    private val nextTabId = AtomicInteger(0)

    fun clearNexTabId() {
        nextTabId.set(0)
    }

    fun createPasteTab(extScope: CoroutineScope, currentProject: CurrentProject) =
        TabCashPaste(nextTabId.getAndIncrement(), extScope, currentProject)


    fun createFileTab(file: File, extScope: CoroutineScope): TabCash {
        if(TypeFile.isImageFile(file.toPath())) {
            return TabCashFileImg(
                id = nextTabId.getAndIncrement(),
                file = file,
                extScope = extScope
            )
        }
        return TabCashFileText(
            id = nextTabId.getAndIncrement(),
            file = file,
            extScope = extScope
        )
    }

    fun createTabFromSnapshot(
        id: Int,
        snap: SnapshotTabCash,
        extScope: CoroutineScope,
        currentProject: CurrentProject,
    ): TabCash? {
        if(id >= nextTabId.get()){
            nextTabId.set(id+1)
        }
        val file = snap.pathStr?.let { File(it) }
        return when(snap.type) {
            TypeFile.PASTE_TAB -> TabCashPaste(
                id = id,
                extScope = extScope,
                currentProject = currentProject,
            )
            TypeFile.TEXT_FILE -> file?.let {
                TabCashFileText(
                    id = id,
                    file = it,
                    extScope = extScope,
                )
            }
            TypeFile.IMG_FILE -> file?.let {
                TabCashFileImg(
                    id = id,
                    file = it,
                    extScope = extScope,
                )
            }
            null -> null
        }
    }
}