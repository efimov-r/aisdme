package non.era.aisdme.configs.dataclass

import kotlinx.serialization.Serializable
import non.era.aisdme.configs.dataclass.child.SnapshotTree
import non.era.aisdme.configs.dataclass.snapshots.SnapshotEditor

@Serializable
data class ProjectConfig(
    val version: Int = 1,
    val treeState: SnapshotTree? = null,
    val keyValue: Map<String, String> = mapOf(),
    val snapshotEditor: SnapshotEditor? = null,
)