package non.era.aisdme.configs.dataclass.snapshots

import kotlinx.serialization.Serializable
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.files.TypeFile
import non.era.aisdme.ui.editor.halfviewmodel.TabCash
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
        fun buildFromOldVersion(
            files: Map<Int, TabCash>?,
            leftTabs: List<Int>?,
            rightTabs: List<Int>?,
            leftActive: Int?,
            rightActive: Int?,
            projectStore: ConfigStore<ProjectConfig>?,
        ): SnapshotEditor? {
            val trueFiles = files?.mapNotNull { (id, tab) ->
                tab.toSnapshot()?.let { snap -> id to snap } //вкладка с ошибкой пропускается, остальные сохраняются
            }?.toMap()
            val snapFromCash = projectStore?.getFromCashe()?.snapshotEditor
            if(snapFromCash == null) { //старой версии нет
                if(trueFiles != null && leftTabs!= null) {
                    val default = SnapshotEditor()
                    Log.d("snapFromCash, new default")
                    return default.copy(
                        files = trueFiles,
                        leftTabs = leftTabs,
                        rightTabs = rightTabs ?: default.rightTabs,
                        leftTabsSelect = leftActive ?: default.leftTabsSelect,
                        rightTabsSelect = rightActive ?: default.rightTabsSelect
                    )
                } else {
                    return null //нехуй сохранять
                }
            } else { //есть старая версия, обновляем
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