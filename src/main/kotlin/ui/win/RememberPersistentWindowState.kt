package non.era.aisdme.ui.win

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.flow.debounce
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.utils.Log

@Composable
fun rememberPersistentWindowState(
    config: AppConfig,
    store: ConfigStore<AppConfig>
): WindowState {
    val state = rememberWindowState(
        size = config.let { Log.d("set size, width: ${it.width.dp}")
            DpSize(it.width.dp, it.height.dp)
            },

        position = config.let {
            if (it.x != null && it.y != null)
                WindowPosition.Absolute(it.x.dp, it.y.dp)
            else WindowPosition.PlatformDefault
        },

        placement = config.placement.let {
            WindowPlacement.valueOf(it)
        }
    )

    LaunchedEffect(state) {
        snapshotFlow {
            Triple(state.size, state.position, state.placement)
        }
            .debounce(4000)
            .collect { (size, pos, placement) ->
                store.update {
                    Log.d("store win update")
                    it.copy(
                        width = size.width.value,
                        height = size.height.value,
                        x = (pos as? WindowPosition.Absolute)?.x?.value,
                        y = (pos as? WindowPosition.Absolute)?.y?.value,
                        placement = placement.name
                    )
                }
            }
    }

    return state
}