package eu.matejkormuth.db2project.ui

object Scene {

    object NoopDrawable : Drawable {
        override fun handleInput(ctx: ConsoleContext) {}
        override fun draw(ctx: ConsoleContext) {}
    }

    private var drawable: Drawable = NoopDrawable
    var content: Drawable
        get() {
            return drawable
        }
        set(value) {
            drawable = value
            redraw()
        }

    private val ctx = ConsoleContext()

    fun clear() {
        ctx.clear()
    }

    private fun redraw() {
        clear()
        drawable.draw(ctx)
        drawable.handleInput(ctx)
    }
}