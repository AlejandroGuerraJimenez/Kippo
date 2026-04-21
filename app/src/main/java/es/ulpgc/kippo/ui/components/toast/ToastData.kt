package es.ulpgc.kippo.ui.components.toast

data class ToastData(
    val id: Long,
    val message: String,
    val type: ToastType,
    val durationMs: Long = 3500L,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)
