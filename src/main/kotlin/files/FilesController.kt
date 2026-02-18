package non.era.aisdme.files

import kotlinx.coroutines.launch
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.ProjectNode

class FilesController : UiController(), IFileController {
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
                if(node is DirNode) {
                    if(node.getFile().exists()) FilesExecutor.deleteRecursive(node.getFilePath())
                } else {
                    if(node.getFile().exists()) FilesExecutor.delete(node.getFilePath())
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

    override fun pasteFromClipboard(targetNode: List<ProjectNode>) {
        if(targetNode.isEmpty()) {
            scope.launch {
                newGlobalError("Directory for paste is not selected")
            }
            return
        }
        if(targetNode.size != 1) {
            scope.launch {
                newGlobalError("Pasting is only available in one directory.")
            }
            return
        }
        val node = targetNode[0]
        if(node !is DirNode) {
            scope.launch {
                newGlobalError("Pasting is only available in directory.")
            }
            return
        }
        safeLaunch {
            FilesExecutor.pasteFromClipboard(node.getFilePath())
        }
    }

    override fun move(src: ProjectNode, dst: ProjectNode) {
        safeLaunch {
            FilesExecutor.move(src.getFilePath(), dst.getFilePath())
        }
    }

//    override fun copy(src: Path, dst: Path) = safeLaunch {
//        FilesExecutor.copy(src, dst)
//    }
}