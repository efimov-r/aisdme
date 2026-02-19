package non.era.aisdme.configs.dataclass

import kotlinx.serialization.Serializable
import non.era.aisdme.configs.dataclass.child.TreeStateConfig

@Serializable
data class ProjectConfig(
    val version: Int = 1,
    val treeState: TreeStateConfig? = null,
)