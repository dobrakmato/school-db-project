package eu.matejkormuth.db2project.ui

data class Text(val text: String) : Drawable {
    override fun draw(ctx: ConsoleContext) {
        ctx.text(text)
    }

    override fun handleInput(ctx: ConsoleContext) {
        // no-op
    }
}