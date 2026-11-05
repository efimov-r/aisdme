package non.era.aisdme.ui.project

import androidx.compose.runtime.mutableStateListOf
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File

class TreeSelection { //по файлам, а не в узлах: переживает пересоздание узла
    private val _files = mutableStateListOf<File>()

    fun isSelected(node: ProjectNode) = node.getFile() in _files

    fun isEmpty() = _files.isEmpty()

    fun set(node: ProjectNode, selected: Boolean) {
        val file = node.getFile()
        if(!selected) _files.remove(file)
        else if(file !in _files) _files.add(file)
    }

    fun clear() = _files.clear()

    fun retain(files: Set<File>) { //файлы, которых больше нет в дереве, — из выделения
        if(_files.any { it !in files }) _files.retainAll(files)
    }
}