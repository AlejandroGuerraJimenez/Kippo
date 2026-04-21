package es.ulpgc.kippo.ui.components.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun ToastHost(modifier: Modifier = Modifier) {
    val toasts by ToastManager.toasts.collectAsState()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = statusBarTop + 8.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val density = LocalDensity.current
        toasts.takeLast(3).forEach { data ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { with(density) { (-40).dp.roundToPx() } }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { with(density) { (-40).dp.roundToPx() } }) + fadeOut()
            ) {
                ToastItem(data = data, onDismiss = { ToastManager.dismiss(data.id) })
            }
        }
    }
}
