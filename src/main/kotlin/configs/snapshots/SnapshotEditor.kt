package non.era.aisdme.configs.dataclass.snapshots

import kotlinx.serialization.Serializable
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.ui.editor.halfviewmodel.ObservableOpenedHalf
import non.era.aisdme.ui.editor.halfviewmodel.ObservableOpenedHalfFile
import non.era.aisdme.utils.Log

@Serializable
data class SnapshotEditor(
    val files: Map<Int, String> = mapOf(),
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
        fun buildFromOldVersion(
            files: Map<Int, ObservableOpenedHalf>?,
            leftTabs: List<Int>?,
            rightTabs: List<Int>?,
            leftActive: Int?,
            rightActive: Int?,
            projectStore: ConfigStore<ProjectConfig>?,
        ): SnapshotEditor? {
            val trueFiles = files?.mapNotNull {
                val half = it.value
                if(half is ObservableOpenedHalfFile) {
                    if(!half.writeStatus.toConfig) return@mapNotNull null
                    it.key to half.getFilePath()
                } else return@mapNotNull null
            }?.toMap()
            val snapFromCash = projectStore?.getFromCashe()?.snapshotEditor
            if(snapFromCash == null) {
                if(trueFiles != null && leftTabs!= null && leftActive != null) {
                    val default = SnapshotEditor()
                    Log.d("snapFromCash, new default")
                    return default.copy(
                        files = trueFiles,
                        leftTabs = leftTabs,
                        rightTabs = rightTabs ?: default.rightTabs,
                        leftTabsSelect = leftActive,
                        rightTabsSelect = rightActive ?: default.rightTabsSelect
                    )
                } else {
                    return null
                }
            } else {
                return snapFromCash.copy(
                    files = trueFiles ?: snapFromCash.files,
                    leftTabs = leftTabs ?: snapFromCash.leftTabs,
                    rightTabs = rightTabs ?: snapFromCash.rightTabs,
                    leftTabsSelect = leftActive ?: snapFromCash.leftTabsSelect,
                    rightTabsSelect = rightActive ?: snapFromCash.rightTabsSelect
                )
            }
        }

    }

}