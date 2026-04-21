package es.ulpgc.kippo.util

class RealtimeDiffer<T>(
    private val keySelector: (T) -> String
) {
    private var firstEmit = true
    private var previous: Map<String, T> = emptyMap()

    fun diff(
        newList: List<T>,
        onAdded: (T) -> Unit = {},
        onUpdated: (prev: T, curr: T) -> Unit = { _, _ -> },
        onRemoved: (T) -> Unit = {}
    ) {
        val newMap = newList.associateBy(keySelector)
        if (firstEmit) {
            firstEmit = false
            previous = newMap
            return
        }
        val addedKeys = newMap.keys - previous.keys
        addedKeys.forEach { k -> newMap[k]?.let(onAdded) }
        val removedKeys = previous.keys - newMap.keys
        removedKeys.forEach { k -> previous[k]?.let(onRemoved) }
        val common = newMap.keys.intersect(previous.keys)
        common.forEach { k ->
            val p = previous[k] ?: return@forEach
            val c = newMap[k] ?: return@forEach
            if (p != c) onUpdated(p, c)
        }
        previous = newMap
    }

    fun reset() {
        firstEmit = true
        previous = emptyMap()
    }
}
