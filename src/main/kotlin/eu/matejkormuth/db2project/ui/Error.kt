package eu.matejkormuth.db2project.ui

data class Error(val text: String, val waitForEnter: Boolean = false) : Drawable {
    override fun draw(ctx: ConsoleContext) {
        ctx.text("")
        ctx.text("Error: ${ctx.colors.red(text)}")
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