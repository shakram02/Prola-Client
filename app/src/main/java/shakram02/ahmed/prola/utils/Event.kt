package shakram02.ahmed.prola.utils

/**
 * C# Style events
 */
class Event<T> {
    private val handlers = arrayListOf<(T) -> Unit>()
    operator fun plusAssign(handler: (T) -> Unit) {
        handlers.add(handler)
    }

    operator fun minusAssign(handler: (T) -> Unit) {
        handlers.remove(handler)
    }

    operator fun invoke(value: T) {
        handlers.forEach { it(value) }
    }

    fun clear() {
        handlers.clear()
    }
}
