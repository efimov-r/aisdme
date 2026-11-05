package non.era.aisdme.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import non.era.aisdme.configs.dataclass.AppConfig

@Composable
fun buildAppController(globalConf: AppConfig) : AppController = remember { AppController(globalConf) }