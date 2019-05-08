package eu.matejkormuth.db2project.ui

import kotlin.text.StringBuilder

/**
 * UI component used to render tables of rows.
 */
class DataTable<Row>(
        private val rows: Iterable<Row>,
        private val header: Iterable<String>,
        private val padding: Int = 6,
        private val rowRender: (Row) -> Iterable<String>
) : Drawable {

    private val columnsWidths: IntArray

    init {
        val itr = header.iterator()
        columnsWidths = IntArray(header.count()) { itr.next().length + padding }
    }

    override fun handleInput(ctx: ConsoleContext) {
        ctx.text(ctx.colors.gray("Press enter to continue."))
        ctx.readLine()
        finish()
    }

    override fun draw(ctx: ConsoleContext) {
        val renderedRows = rows.map {
            val row = rowRender(it)
            row.forEachIndexed { idx, cell ->
                columnsWidths[idx] = Math.max(columnsWidths[idx], cell.length + padding)
            }
            row
        }

        ctx.text("") /* empty line can never be bad */
        tableHeader(ctx)
        printSeparator(ctx)
        renderedRows.forEach { ctx.text(transformToRow(it)) }
        printSeparator(ctx)
    }

    /**
     * Prints table header.
     */
    private fun tableHeader(ctx: ConsoleContext) = ctx.text(transformToRow(header))

    /**
     * Prints table separator.
     */
    private fun printSeparator(ctx: ConsoleContext) = ctx
            .text(columnsWidths.joinToString("+") { "-".repeat(it) })

    /**
     * Transforms iterable of string to table row.
     */
    private fun transformToRow(items: Iterable<String>) = items
            .mapIndexed { idx, it -> padCenter(it, columnsWidths[idx]) }
            .joinToString("|")

    /**
     * Pads specified string to center with specified padding character and width.
     */
    private fun padCenter(str: String, width: Int, char: Char = ' '): String {
        val whitespace = width - str.length
        val left = Math.floor(whitespace / 2.0).toInt()
        val right = Math.ceil(whitespace / 2.0).toInt()

        val builder = StringBuilder()
        repeat(left) { builder.append(char) }
        builder.append(str)
        repeat(right) { builder.append(char) }

        return builder.toString()
    }

}