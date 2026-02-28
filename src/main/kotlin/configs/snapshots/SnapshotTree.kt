package non.era.aisdme.configs.dataclass.child

import kotlinx.serialization.Serializable

@Serializable
data class TreeStateConfig(
    val treeState: Map<String, TreeStateElement> = mapOf()
)

@Serializable
data class TreeStateElement(
    val isDeer: Boolean = false,
    val expanded: Boolean = false,
) {
}