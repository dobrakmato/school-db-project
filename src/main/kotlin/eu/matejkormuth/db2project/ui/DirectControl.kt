package eu.matejkormuth.db2project.ui

/**
 * UI component for implementing "anonymous" ui components.
 */
data class DirectControl(val receiver: (ConsoleContext) -> Unit) : Drawable {
    override fun draw(ctx: ConsoleContext) {
        receiver(ctx)
    }

    override fun handleInput(ctx: ConsoleContext) {
        finish()
    }
}