package eu.matejkormuth.db2project.ui

data class DirectControl(val receiver: (ConsoleContext) -> Unit) : Drawable {
    override fun draw(ctx: ConsoleContext) {
        receiver(ctx)
    }

    override fun handleInput(ctx: ConsoleContext) {}
}