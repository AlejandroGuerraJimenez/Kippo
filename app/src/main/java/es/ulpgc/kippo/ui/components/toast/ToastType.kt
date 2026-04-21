package es.ulpgc.kippo.ui.components.toast

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToastType(
    val background: Color,
    val accent: Color,
    val icon: ImageVector
) {
    SUCCESS(
        background = Color(0xFF1F8F7A),
        accent = Color(0xFFB9F0E4),
        icon = Icons.Filled.CheckCircle
    ),
    ERROR(
        background = Color(0xFFB3261E),
        accent = Color(0xFFFFD9D6),
        icon = Icons.Filled.Error
    ),
    INFO(
        background = Color(0xFF102623),
        accent = Color(0xFFE0EFEC),
        icon = Icons.Filled.Info
    ),
    REALTIME(
        background = Color(0xFF2E6BDA),
        accent = Color(0xFFD6E4FF),
        icon = Icons.Filled.Notifications
    )
}
