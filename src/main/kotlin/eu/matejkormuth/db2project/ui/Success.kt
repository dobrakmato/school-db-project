package eu.matejkormuth.db2project.ui

/**
 * UI component for rendering success messages.
 */
data class Success(val text: String, val waitForEnter: Boolean = false) : Drawable {
    override fun draw(ctx: ConsoleContext) {
        ctx.text("")
        ctx.text("Success: ${ctx.colors.green(text)}")
        ctx.text("")
        ctx.text(ctx.colors.gray("Press enter to continue."))
        readLine()
    }

    override fun handleInput(ctx: ConsoleContext) {
        if (waitForEnter) {
            ctx.readLine()
        }
        finish()
    }
}