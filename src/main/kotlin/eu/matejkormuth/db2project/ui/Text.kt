package eu.matejkormuth.db2project.ui

data class Text(val text: String, val waitForEnter: Boolean = false) : Drawable {
    override fun draw(ctx: ConsoleContext) {
        ctx.text(text)
    }

    override fun handleInput(ctx: ConsoleContext) {
        if (waitForEnter) {
            ctx.readLine()
        }
        finish()
    }
}