package non.era.aisdme.files.work

import kotlinx.coroutines.launch
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.DeleteDir
import non.era.aisdme.bus.DeleteFile
import non.era.aisdme.bus.MoveFile
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.ProjectNode
import non.era.aisdme.utils.Log
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class FilesController(
    private val bus: AppEventBus,
) : UiController(), IFileController {
    override fun createFile(nodes: List<ProjectNode>, name: String) {
        if(nodes.isEmpty()) {
            scope.launch {
                newGlobalError("Directory for file creation is not selected")
            }
            return
        }
        if(nodes.size != 1) {
            scope.launch {
                newGlobalError("Create a file in only one directory")
            }
            return
        }
        val node = nodes[0]
        if(node !is DirNode) {
            scope.launch {
                newGlobalError("Create a file in only directory")
            }
            return
        }
        safeLaunch {
            FilesExecutor.createFile(node.getFilePath().resolve(name))
        }
    }

    override fun createDirectory(nodes: List<ProjectNode>, name: String) {
        if(nodes.isEmpty()) {
            scope.launch {
                newGlobalError("Directory for directory creation is not selected")
            }
            return
        }
        if(nodes.size != 1) {
            scope.launch {
                newGlobalError("Create a directory in only one directory")
            }
            return
        }
        val node = nodes[0]
        if(node !is DirNode) {
            scope.launch {
                newGlobalError("Create a file in only directory")
            }
            return
        }
        safeLaunch {
            FilesExecutor.createDirectory(node.getFilePath().resolve(name))
        }
    }

    override fun delete(nodes: List<ProjectNode>) {
        if(nodes.isEmpty()) {
            scope.launch {
                newGlobalError("Directory or file for delete is not selected")
            }
            return
        }
        safeLaunch {
            nodes.forEach { node->
                val path = node.getFilePath()
                if(node is DirNode) {
                    if(node.getFile().exists()) {
                        FilesExecutor.deleteRecursive(path)
                        bus.publish(DeleteDir(path))
                    }
                } else {
                    if(node.getFile().exists()) {
                        FilesExecutor.delete(path)
                        bus.publish(DeleteFile(path))
                    }
                }
            }
        }
    }

    private fun areAllInSameDirectory(nodes: List<ProjectNode>): Boolean {
        if (nodes.isEmpty()) return true
        val firstParent = nodes.first().getFile().parent
        return nodes.all { it.getFile().parent == firstParent }
    }

    override fun copyToClipboard(nodes: List<ProjectNode>, cut: Boolean) {
        if(nodes.isEmpty()) {
            scope.launch {
                newGlobalError("Directory or file for copy/cut is not selected")
            }
            return
        }
        if(!areAllInSameDirectory(nodes)) {
            scope.launch {
                newGlobalError("Files must be in the same directory")
            }
            return
        }
        FilesExecutor.copyToClipboard(nodes.map{it.getFilePath()}, cut)
    }

    override fun pasteFromClipboard(target: Path) {
        safeLaunch {
            FilesExecutor.pasteFromClipboard(target)
        }
    }

    override fun move(src: List<ProjectNode>, dst: File) {
        src.forEach {
            safeLaunch {
                Log.d("move, src=$src, dst=$dst")
                val target = if (Files.isDirectory(dst.toPath())) {
                    dst.toPath().resolve(it.getFilePath().fileName)
                } else {
                    dst.toPath()
                }
                FilesExecutor.move(it.getFilePath(), target)
                bus.publish(MoveFile(it.getFilePath(), target))
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