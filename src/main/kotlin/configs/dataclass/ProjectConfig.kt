package non.era.aisdme.configs.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class ProjectConfig(
    val version: Int = 1,
)