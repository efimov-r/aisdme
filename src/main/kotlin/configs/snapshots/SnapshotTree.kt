package non.era.aisdme.configs.dataclass.child

import kotlinx.serialization.Serializable
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.utils.Log

@Serializable
data class SnapshotTree(
    val treeState: Map<String, TreeStateElement> = mapOf()
) {
    suspend fun saveToConfig(projectStore: ConfigStore<ProjectConfig>?) {
        projectStore?.update {
            Log.d("Save tree snapshot, dispose or reselect proj")
            it.copy(treeState = this)
        }
    }
}

@Serializable
data class TreeStateElement(
    val isDeer: Boolean = false,
    val expanded: Boolean = false,
)