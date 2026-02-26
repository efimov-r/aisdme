package non.era.aisdme.ui.editor

import kotlinx.serialization.Serializable

@Serializable
data class SnapshotEditor(
    val files: Map<Int, String>,
    val leftTabs: List<Int>,
    val leftTabsSelect: Int?,
    val rightTabs: List<Int>,
    val rightTabsSelect: Int?,
) {
    companion object {
        fun createSnapshot(
            files: Map<Int, ObservableOpenedFile>,
            leftTabs: HalfEditorTabs,
            rightTabs: HalfEditorTabs
        ): SnapshotEditor {
            val notSaveIds = files.mapNotNull {
                if(!it.value.writeStatus.toConfig) return@mapNotNull it.key
                return@mapNotNull null
            }

            val leftTabsFinish = leftTabs.listTabs.toMutableList()
            leftTabsFinish.removeAll(notSaveIds)

            val leftTabsSelectFinish = if(notSaveIds.contains(leftTabs.getActiveTabId())) {
                leftTabsFinish.lastOrNull()
            } else {
                leftTabs.getActiveTabId()
            }

            val rightTabsFinish = rightTabs.listTabs.toMutableList()
            rightTabsFinish.removeAll(notSaveIds)

            val rightTabsSelectFinish = if(notSaveIds.contains(rightTabs.getActiveTabId())) {
                rightTabsFinish.lastOrNull()
            } else {
                rightTabs.getActiveTabId()
            }

            return SnapshotEditor(
                files = files.mapNotNull {
                    if(notSaveIds.contains(it.key)) return@mapNotNull null
                    it.key to it.value.getFilePath()
                }.toMap(),
                leftTabs = leftTabsFinish,
                leftTabsSelect = leftTabsSelectFinish,
                rightTabs = rightTabsFinish,
                rightTabsSelect = rightTabsSelectFinish
            )
        }
    }
}

