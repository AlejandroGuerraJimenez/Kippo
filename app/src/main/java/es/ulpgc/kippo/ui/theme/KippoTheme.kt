package es.ulpgc.kippo.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import es.ulpgc.kippo.ui.KippoColors

private val KippoLightColorScheme = lightColorScheme(
    primary = KippoColors.Teal,
    onPrimary = Color.White,
    secondary = KippoColors.DarkTeal,
    onSecondary = Color.White,
    tertiary = KippoColors.Yellow,
    onTertiary = KippoColors.DarkText,
    background = KippoColors.Background,
    onBackground = KippoColors.DarkText,
    surface = Color.White,
    onSurface = KippoColors.DarkText
)

@Composable
fun KippoTheme(content: @Composable () -> Unit) {
    val colorScheme = KippoLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            val lightStatus = colorScheme.background.luminance() > 0.5f
            val lightNav = colorScheme.background.luminance() > 0.5f
            controller.isAppearanceLightStatusBars = lightStatus
            controller.isAppearanceLightNavigationBars = lightNav
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
