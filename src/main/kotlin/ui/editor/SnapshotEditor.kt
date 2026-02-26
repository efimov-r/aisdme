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
        ) = SnapshotEditor(
                files = files.map { it.key to it.value.getFilePath() }.toMap(),
                leftTabs = leftTabs.listTabs,
                leftTabsSelect = leftTabs.getActiveTabId(),
                rightTabs = rightTabs.listTabs,
                rightTabsSelect = rightTabs.getActiveTabId()
            )
    }
}

