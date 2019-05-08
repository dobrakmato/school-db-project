package eu.matejkormuth.db2project.ui

/**
 * Base interface for all UI components.
 */
interface Drawable {
    /**
     * Draws this component using provided console context.
     */
    fun draw(ctx: ConsoleContext)

    /**
     * Handles input of this component using provided context. (optional operation)
     */
    fun handleInput(ctx: ConsoleContext)

    /**
     * Tries to pop this item from scene stack.
     */
    fun finish() {
        Scene.pop(this)
    }
}
