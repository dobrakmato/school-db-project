package eu.matejkormuth.db2project.ui

import java.util.*

data class Menu(val items: List<MenuItem>, val header: String? = null) : Drawable {

    override fun draw(ctx: ConsoleContext) {
        header?.let { ctx.text(it, end = "\n\n") }
        items.forEachIndexed { index, item ->
            ctx.text(formatItem(index, item))
        }
        ctx.text("")
    }

    override fun handleInput(ctx: ConsoleContext) {
        var optional = Optional.empty<Int>()
        while (!optional.isPresent) {
            ctx.prompt("> Select your choice (enter a number): ")
            optional = ctx.readInt(minValue = 0, maxValue = items.size)
        }

        items[optional.get() - 1].onChoose()
    }

    private fun formatItem(index: Int, item: MenuItem): String {
        return "${index + 1}. ${item.text}"
    }
}