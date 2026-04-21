package es.ulpgc.kippo.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import es.ulpgc.kippo.ui.KippoColors

/**
 * Scaffold corporativo que garantiza que el contenido respeta siempre el área
 * segura de la pantalla: status bar, barra de navegación (gestual o táctil en
 * Xiaomi), cutout de cámara y teclado IME.
 *
 * Regla del proyecto: ninguna pantalla aplica `windowInsetsPadding` manualmente
 * ni usa `Scaffold` directamente — siempre se usa `KippoScaffold`.
 *
 * El `content` recibe `PaddingValues` que YA incluye el espacio consumido por
 * topBar/bottomBar. Es obligatorio propagarlos al contenedor principal
 * (`Modifier.padding(padding)`) o a `LazyColumn#contentPadding`.
 */
@Composable
fun KippoScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = KippoColors.Background,
    contentWindowInsets: WindowInsets = KippoWindowInsets.safeContent(),
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentWindowInsets = contentWindowInsets,
        content = content
    )
}

object KippoWindowInsets {
    /**
     * Insets seguros para contenido de pantalla: status bar + nav bar + cutout.
     * No incluye IME — se gestiona en diálogos/bottom sheets con `imePadding()`.
     */
    @Composable
    fun safeContent(): WindowInsets =
        WindowInsets.statusBars
            .union(WindowInsets.navigationBars)
            .union(WindowInsets.displayCutout)

    /**
     * Insets para overlays (ModalBottomSheet, Dialog): nav bar + IME.
     * Deja que el contenido respete el teclado y la barra de navegación.
     */
    @Composable
    fun bottomOverlay(): WindowInsets =
        WindowInsets.navigationBars.union(WindowInsets.ime)
}
