package non.era.aisdme.configs.dataclass

import kotlinx.serialization.Serializable
import non.era.aisdme.configs.dataclass.child.TreeStateElement

@Serializable
data class ProjectConfig(
    val version: Int = 1,
    val treeState: Map<String, TreeStateElement> = mapOf(),
)