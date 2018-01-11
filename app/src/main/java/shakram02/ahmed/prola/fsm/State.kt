package fsm

import shakram02.ahmed.prola.fsm.Edge

/**
 *
 */
class State(val name: BaseState) {
    private val edges = hashMapOf<BaseEvent, Edge>()   // Convert to HashMap with event as key
    private val stateActions = mutableListOf<(State) -> Unit>()

    /**
     * Creates an edge from a [State] to another when a [BaseEvent] occurs
     * @param event: Transition event
     * @param targetState: Next state
     * @param init: I find it as weird as you do, here you go https://kotlinlang.org/docs/reference/lambdas.html
     */
    fun edge(event: BaseEvent, targetState: BaseState, init: Edge.() -> Unit) {
        val edge = Edge(event, targetState)
        edge.init()

        if (edges.containsKey(event)) {
            throw DFAError("Adding multiple edges for the same event is invalid")
        }

        edges.put(event, edge)
    }

    /**
     * Action performed by state
     */
    fun action(action: (State) -> Unit) {
        stateActions.add(action)
    }

    /**
     * Enter the state and run all actions
     */
    fun enter() {
        // Every action takes the current state
        stateActions.forEach { it(this) }
    }

    /**
     * Get the appropriate [Edge] for the [BaseEvent]
     */
    fun getEdgeForEvent(event: BaseEvent): Edge {
        try {
            return edges[event]!!
        } catch (e: KotlinNullPointerException) {
            throw IllegalStateException("Event $event isn't registered with state ${this.name}")
        }
    }

    override fun toString(): String {
        return name.javaClass.simpleName
    }

}
