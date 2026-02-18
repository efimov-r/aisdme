package non.era.aisdme.configs.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val version: Int = 1,
    val width: Float = 1200f,
    val height: Float = 800f,
    val x: Float? = null,
    val y: Float? = null,
    val placement: String = "Floating",

    val recentProjects: List<String> = emptyList(),
    val theme: String = "dark"
)