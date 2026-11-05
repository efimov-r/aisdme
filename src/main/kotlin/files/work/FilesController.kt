package non.era.aisdme.files.work

import kotlinx.coroutines.launch
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.DeleteDir
import non.era.aisdme.bus.DeleteFile
import non.era.aisdme.bus.MoveFile
import non.era.aisdme.ui.UiController
import non.era.aisdme.utils.Log
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

class FilesController(
    private val bus: AppEventBus,
) : UiController(), IFileController {
    override fun createFile(dir: Path, name: String) {
        safeLaunch {
            FilesExecutor.createFile(dir.resolve(name))
        }
    }

    override fun createDirectory(dir: Path, name: String) {
        safeLaunch {
            FilesExecutor.createDirectory(dir.resolve(name))
        }
    }

    override fun delete(paths: List<Path>) {
        if(paths.isEmpty()) {
            scope.launch {
                newGlobalError("Directory or file for delete is not selected")
            }
            return
        }
        safeLaunch {
            paths.forEach { path->
                if(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) { //ссылка на папку удаляется как файл
                    FilesExecutor.deleteRecursive(path)
                    bus.publish(DeleteDir(path))
                } else if(Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                    FilesExecutor.delete(path)
                    bus.publish(DeleteFile(path))
                }
            }
        }
    }

    private fun areAllInSameDirectory(paths: List<Path>): Boolean {
        if (paths.isEmpty()) return true
        val firstParent = paths.first().parent
        return paths.all { it.parent == firstParent }
    }

    override fun copyToClipboard(paths: List<Path>, cut: Boolean) {
        if(paths.isEmpty()) {
            scope.launch {
                newGlobalError("Directory or file for copy/cut is not selected")
            }
            return
        }
        if(!areAllInSameDirectory(paths)) {
            scope.launch {
                newGlobalError("Files must be in the same directory")
            }
            return
        }
        FilesExecutor.copyToClipboard(paths, cut)
    }

    override fun pasteFromClipboard(target: Path) {
        safeLaunch {
            FilesExecutor.pasteFromClipboard(target).forEach { (src, dst) ->
                bus.publish(MoveFile(src, dst))
            }
        }
    }

    override fun move(src: List<Path>, dst: Path) {
        src.forEach {
            safeLaunch {
                Log.d("move, src=$src, dst=$dst")
                val target = if (Files.isDirectory(dst)) dst.resolve(it.fileName) else dst
                FilesExecutor.move(it, target)
                bus.publish(MoveFile(it, target))
            }
        }
    }

    override fun rename(src: Path, newName: String) {
        safeLaunch {
            val target = src.resolveSibling(newName)
            FilesExecutor.move(src, target)
            bus.publish(MoveFile(src, target))
        }
    }

//    override fun copy(src: Path, dst: Path) = safeLaunch {
//        FilesExecutor.copy(src, dst)
//          bus.publish
//    }
}