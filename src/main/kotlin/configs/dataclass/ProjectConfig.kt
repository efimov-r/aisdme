package non.era.aisdme.configs.dataclass

import kotlinx.serialization.Serializable
import non.era.aisdme.configs.dataclass.child.TreeStateConfig
import non.era.aisdme.configs.snapshots.SnapshotEditor

@Serializable
data class ProjectConfig(
    val version: Int = 1,
    val treeState: TreeStateConfig? = null,
    val keyValue: Map<String, String> = mapOf(),
    val snapshotEditor: SnapshotEditor? = null,
)