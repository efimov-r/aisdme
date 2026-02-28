package non.era.aisdme.configs.dataclass.snapshots

import kotlinx.serialization.Serializable

@Serializable
data class SnapshotEditor(
    val files: Map<Int, String> = mapOf(),
    val leftTabs: List<Int> = listOf(),
    val leftTabsSelect: Int? = null,
    val rightTabs: List<Int> = listOf(),
    val rightTabsSelect: Int? = null,
)