package eu.matejkormuth.db2project.ui

import com.github.ajalt.mordant.TermColors
import java.util.*

data class Menu(val items: List<MenuItem>, val header: String? = null, val allowBack: Boolean = true) : Drawable {

    override fun draw(ctx: ConsoleContext) {
        /* clear terminal before displaying menu */
        ctx.clear()

        header?.let { ctx.text(ctx.colors.brightBlue(it), end = "\n\n") }
        items.forEachIndexed { index, item ->
            ctx.text(formatItem(index, item, ctx.colors))
        }

        if (allowBack) {
            ctx.text("")
            ctx.text(ctx.colors.gray("Or choose 0 to go back."))
        }
        ctx.text("")
    }

    override fun handleInput(ctx: ConsoleContext) {
        var optional = Optional.empty<Int>()
        while (!optional.isPresent) {
            ctx.prompt("> Select your choice (enter a number): ")
            optional = ctx.readInt(minValue = if (allowBack) 0 else 1, maxValue = items.size + 1) // + 1 because were counting from 1
        }

        /* check for exit request */
        if (allowBack && optional.get() == 0) {
            return finish()
        }

        items[optional.get() - 1].onChoose()
    }

    private fun formatItem(index: Int, item: MenuItem, colors: TermColors): String {
        return "${colors.brightYellow((index + 1).toString())}. ${item.text}"
    }
}