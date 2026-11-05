package non.era.aisdme.configs.dataclass.snapshots

import kotlinx.serialization.Serializable
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.files.TypeFile
import non.era.aisdme.utils.Log

@Serializable
data class SnapshotTabCash(
    val pathStr: String? = null,
    val type: TypeFile? = null,
)

@Serializable
data class SnapshotEditor(
    val files: Map<Int, SnapshotTabCash> = mapOf(),
    val leftTabs: List<Int> = listOf(),
    val leftTabsSelect: Int? = null,
    val rightTabs: List<Int> = listOf(),
    val rightTabsSelect: Int? = null,
) {
    suspend fun saveToConfig(projectStore: ConfigStore<ProjectConfig>?) {
        projectStore?.update {
            Log.d("Save editor snapshot, dispose or reselect proj")
            it.copy(snapshotEditor = this)
        }
    }

    companion object {
        fun buildFromOldVersion( //старый снимок обновляется тем, что есть в репозитории
            rep: EditorStateRepository,
            projectStore: ConfigStore<ProjectConfig>?,
        ): SnapshotEditor? {
            val files = rep.files.value
            val leftTabs = rep.leftTabs.value
            val old = projectStore?.getFromCashe()?.snapshotEditor
                ?: if (files != null && leftTabs != null) SnapshotEditor() else return null //сохранять нечего

            return old.copy(
                files = files ?: old.files,
                leftTabs = leftTabs ?: old.leftTabs,
                rightTabs = rep.rightTabs.value ?: old.rightTabs,
                leftTabsSelect = rep.leftActive.value ?: old.leftTabsSelect,
                rightTabsSelect = rep.rightActive.value ?: old.rightTabsSelect
            )
        }

    }

}