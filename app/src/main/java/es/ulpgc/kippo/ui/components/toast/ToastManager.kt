package es.ulpgc.kippo.ui.components.toast

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

object ToastManager {
    private val idGen = AtomicLong(0L)

    private val _toasts = MutableStateFlow<List<ToastData>>(emptyList())
    val toasts: StateFlow<List<ToastData>> = _toasts.asStateFlow()

    fun show(
        message: String,
        type: ToastType = ToastType.INFO,
        durationMs: Long = 3500L,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ): Long {
        if (message.isBlank()) return -1L
        val id = idGen.incrementAndGet()
        val toast = ToastData(id, message, type, durationMs, actionLabel, onAction)
        _toasts.value = _toasts.value + toast
        return id
    }

    fun showSuccess(message: String) = show(message, ToastType.SUCCESS)
    fun showError(message: String) = show(message, ToastType.ERROR, durationMs = 4500L)
    fun showInfo(message: String) = show(message, ToastType.INFO)
    fun showRealtime(message: String) = show(message, ToastType.REALTIME)

    fun dismiss(id: Long) {
        _toasts.value = _toasts.value.filterNot { it.id == id }
    }

    fun clear() {
        _toasts.value = emptyList()
    }
}
